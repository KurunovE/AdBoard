package com.solarlab.adboard.service;

import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.auth.LogoutRequest;
import com.solarlab.adboard.dto.request.auth.RefreshTokenRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakCredentialRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakLoginRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakLogoutRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakRefreshTokenRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakUserCreateRequest;
import com.solarlab.adboard.dto.request.user.UserRegistrationRequest;
import com.solarlab.adboard.dto.response.auth.LoginResponse;
import com.solarlab.adboard.dto.response.user.UserRegistrationResponse;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest registrationRequest) {
        if (userRepository.findByEmail(registrationRequest.email()).isPresent()) {
            log.warn("Registration rejected for existing email={}", registrationRequest.email());
            throw new IllegalArgumentException(
                    "User with email " + registrationRequest.email() + " already exists"
            );
        }

        String keycloakUserId = keycloakAdminService.createUser(
                buildKeycloakUserCreateRequest(registrationRequest));

        try {
            keycloakAdminService.assignClientRoleToUser(keycloakUserId, "USER");
            User savedUser = userRepository.save(buildLocalUser(registrationRequest));
            applicationEventPublisher.publishEvent(new UserRegisteredEvent(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail()
            ));
            log.info("Registered user email={} localId={}", savedUser.getEmail(), savedUser.getId());
            return userMapper.toUserRegistrationResponse(savedUser);
        } catch (RuntimeException ex) {
            log.warn("Registration failed after Keycloak user creation for email={}, rolling back Keycloak userId={}",
                    registrationRequest.email(), keycloakUserId);
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

        KeycloakRefreshTokenRequest keycloakRefreshTokenRequest = KeycloakRefreshTokenRequest.builder()
                .grantType("refresh_token")
                .clientId(keycloakProperties.clientId())
                .refreshToken(refreshTokenRequest.refreshToken())
                .clientSecret(keycloakProperties.clientSecret())
                .build();

        HttpEntity<?> request = new HttpEntity<>(keycloakRefreshTokenRequest.toFormData(), headers);

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

        KeycloakLogoutRequest keycloakLogoutRequest = KeycloakLogoutRequest.builder()
                .clientId(keycloakProperties.clientId())
                .refreshToken(logoutRequest.refreshToken())
                .clientSecret(keycloakProperties.clientSecret())
                .build();

        HttpEntity<?> request = new HttpEntity<>(keycloakLogoutRequest.toFormData(), headers);

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
            UserRegistrationRequest registrationRequest
    ) {
        return new KeycloakUserCreateRequest(
                registrationRequest.email(),
                registrationRequest.email(),
                true,
                true,
                List.of(),
                List.of(new KeycloakCredentialRequest(
                        "password",
                        registrationRequest.password(),
                        false
                ))
        );
    }

    private User buildLocalUser(UserRegistrationRequest registrationRequest) {
        return User.builder()
                .name(registrationRequest.name())
                .email(registrationRequest.email())
                .phone(registrationRequest.phone())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
