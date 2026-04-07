package com.solarlab.adboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.user.UserRequestRegistration;
import com.solarlab.adboard.dto.request.keycloak.KeycloakCredentialRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakUserCreateRequest;
import com.solarlab.adboard.dto.response.auth.LoginResponse;
import com.solarlab.adboard.dto.response.user.UserResponseRegistration;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakAdminService keycloakAdminService;
    private final ObjectMapper objectMapper;

    private static final String KEYCLOAK_PASSWORD_PLACEHOLDER = "{keycloak}";
    private static final String KEYCLOAK_PHONE_PLACEHOLDER = null;

    @Transactional
    public UserResponseRegistration registerUser(UserRequestRegistration userRequestRegistration) {
        if (userRepository.findByEmail(userRequestRegistration.email()).isPresent()) {
            throw new IllegalArgumentException(
                    "User with email " + userRequestRegistration.email() + " already exists"
            );
        }

        String keycloakUserId = keycloakAdminService.createUser(
                buildKeycloakUserCreateRequest(userRequestRegistration));

        try {
            keycloakAdminService.assignClientRoleToUser(keycloakUserId, "USER");
            User savedUser = userRepository.save(buildLocalUser(userRequestRegistration));
            return userMapper.toUserResponseRegistration(savedUser);
        } catch (RuntimeException ex) {
            keycloakAdminService.deleteUserById(keycloakUserId);
            if (ex instanceof DataIntegrityViolationException dataIntegrityViolationException) {
                throw new IllegalArgumentException(
                        "User could not be saved in local database: "
                                + dataIntegrityViolationException.getMostSpecificCause().getMessage()
                );
            }
            throw ex;
        }
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", keycloakProperties.clientId());
        map.add("username", loginRequest.email());
        map.add("password", loginRequest.password());
        map.add("scope", "openid");
        if (hasText(keycloakProperties.clientSecret())) {
            map.add("client_secret", keycloakProperties.clientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    keycloakProperties.tokenUrl(),
                    request,
                    LoginResponse.class
            );

            LoginResponse loginResponse = response.getBody();
            if (loginResponse == null || !hasText(loginResponse.accessToken())) {
                throw new IllegalStateException("Keycloak returned an empty login response");
            }

            synchronizeLocalUser(loginResponse.accessToken(), loginRequest.password());
            return loginResponse;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            throw ex;
        }
    }

    private void synchronizeLocalUser(String accessToken, String rawPassword) {
        Map<String, Object> claims = extractClaims(accessToken);

        String email = getStringClaim(claims, "email");
        if (!hasText(email)) {
            email = getStringClaim(claims, "preferred_username");
        }
        if (!hasText(email)) {
            throw new IllegalStateException("Email claim is missing in access token");
        }

        String name = getStringClaim(claims, "name");
        if (!hasText(name)) {
            name = getStringClaim(claims, "given_name");
        }
        if (!hasText(name)) {
            name = email;
        }

        final String resolvedEmail = email;
        final String resolvedName = name;
        userRepository.findByEmail(resolvedEmail)
                .map(existingUser -> updateExistingUser(
                        existingUser, resolvedName, rawPassword))
                .orElseGet(() -> createLocalUser(resolvedEmail, resolvedName, rawPassword));
    }

    private KeycloakUserCreateRequest buildKeycloakUserCreateRequest(
            UserRequestRegistration userRequestRegistration
    ) {
        return new KeycloakUserCreateRequest(
                userRequestRegistration.email(),
                userRequestRegistration.email(),
                true,
                true,
                List.of(),
                List.of(new KeycloakCredentialRequest(
                        "password",
                        userRequestRegistration.password(),
                        false
                ))
        );
    }

    private User buildLocalUser(UserRequestRegistration userRequestRegistration) {
        return User.builder()
                .name(userRequestRegistration.name())
                .email(userRequestRegistration.email())
                .phone(userRequestRegistration.phone())
                .password(userRequestRegistration.password())
                .build();
    }

    private User updateExistingUser(
            User existingUser,
            String name,
            String rawPassword
    ) {
        existingUser.setName(name);
        if (!hasText(existingUser.getPassword())) {
            existingUser.setPassword(fallbackPassword(rawPassword));
        }
        if (!hasText(existingUser.getPhone())) {
            existingUser.setPhone(KEYCLOAK_PHONE_PLACEHOLDER);
        }
        return userRepository.save(existingUser);
    }

    private User createLocalUser(String email, String name, String rawPassword) {
        User user = User.builder()
                .name(name)
                .email(email)
                .phone(KEYCLOAK_PHONE_PLACEHOLDER)
                .password(fallbackPassword(rawPassword))
                .build();
        return userRepository.save(user);
    }

    private Map<String, Object> extractClaims(String accessToken) {
        try {
            String[] tokenParts = accessToken.split("\\.");
            if (tokenParts.length < 2) {
                throw new IllegalArgumentException("Access token has invalid format");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(tokenParts[1]);
            return objectMapper.readValue(
                    new String(decodedPayload, StandardCharsets.UTF_8),
                    new TypeReference<>() {
                    }
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read access token claims", ex);
        }
    }

    private String getStringClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        return value instanceof String stringValue ? stringValue : null;
    }

    private String fallbackPassword(String rawPassword) {
        return hasText(rawPassword) ? rawPassword : KEYCLOAK_PASSWORD_PLACEHOLDER;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
