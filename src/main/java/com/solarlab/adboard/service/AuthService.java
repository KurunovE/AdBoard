package com.solarlab.adboard.service;

import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakCredentialRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakUserCreateRequest;
import com.solarlab.adboard.dto.request.user.UserRequestRegistration;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakAdminService keycloakAdminService;

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

            return loginResponse;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            throw ex;
        }
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
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
