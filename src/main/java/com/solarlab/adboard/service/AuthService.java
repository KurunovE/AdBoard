package com.solarlab.adboard.service;

import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.auth.LogoutRequest;
import com.solarlab.adboard.dto.request.auth.RefreshTokenRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakCredentialRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakLoginRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakUserCreateRequest;
import com.solarlab.adboard.dto.request.user.UserRequestRegistration;
import com.solarlab.adboard.dto.response.auth.LoginResponse;
import com.solarlab.adboard.dto.response.user.UserResponseRegistration;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            log.warn("Registration rejected for existing email={}", userRequestRegistration.email());
            throw new IllegalArgumentException(
                    "User with email " + userRequestRegistration.email() + " already exists"
            );
        }

        String keycloakUserId = keycloakAdminService.createUser(
                buildKeycloakUserCreateRequest(userRequestRegistration));

        try {
            keycloakAdminService.assignClientRoleToUser(keycloakUserId, "USER");
            User savedUser = userRepository.save(buildLocalUser(userRequestRegistration));
            log.info("Registered user email={} localId={}", savedUser.getEmail(), savedUser.getId());
            return userMapper.toUserResponseRegistration(savedUser);
        } catch (RuntimeException ex) {
            log.warn("Registration failed after Keycloak user creation for email={}, rolling back Keycloak userId={}",
                    userRequestRegistration.email(), keycloakUserId);
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

        KeycloakLoginRequest keycloakLoginRequest = KeycloakLoginRequest.builder()
                .grantType("password")
                .clientId(keycloakProperties.clientId())
                .username(loginRequest.email())
                .password(loginRequest.password())
                .scope("openid")
                .clientSecret(keycloakProperties.clientSecret())
                .build();

        HttpEntity<?> request = new HttpEntity<>(keycloakLoginRequest.toFormData(), headers);

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

            log.info("Successful login for email={}", loginRequest.email());
            return loginResponse;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                log.warn("Login rejected for email={}", loginRequest.email());
                throw new IllegalArgumentException("Invalid email or password");
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", keycloakProperties.clientId());
        formData.add("refresh_token", refreshTokenRequest.refreshToken());

        if (hasText(keycloakProperties.clientSecret())) {
            formData.add("client_secret", keycloakProperties.clientSecret());
        }

        HttpEntity<?> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    keycloakProperties.tokenUrl(),
                    request,
                    LoginResponse.class
            );

            LoginResponse loginResponse = response.getBody();
            if (loginResponse == null || !hasText(loginResponse.accessToken())) {
                throw new IllegalStateException("Keycloak returned an empty refresh response");
            }

            log.info("Successful token refresh");
            return loginResponse;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                log.warn("Refresh token rejected");
                throw new IllegalArgumentException("Invalid or expired refresh token");
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public void logout(LogoutRequest logoutRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", keycloakProperties.clientId());
        formData.add("refresh_token", logoutRequest.refreshToken());

        if (hasText(keycloakProperties.clientSecret())) {
            formData.add("client_secret", keycloakProperties.clientSecret());
        }

        HttpEntity<?> request = new HttpEntity<>(formData, headers);

        try {
            restTemplate.postForEntity(
                    keycloakProperties.logoutUrl(),
                    request,
                    Void.class
            );
            log.info("Successful logout");
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                log.warn("Logout rejected due to invalid refresh token");
                throw new IllegalArgumentException("Invalid or expired refresh token");
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
