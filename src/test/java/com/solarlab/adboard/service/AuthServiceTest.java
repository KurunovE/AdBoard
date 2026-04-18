package com.solarlab.adboard.service;

import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.auth.LogoutRequest;
import com.solarlab.adboard.dto.request.auth.RefreshTokenRequest;
import com.solarlab.adboard.dto.request.user.UserRequestRegistration;
import com.solarlab.adboard.dto.response.auth.LoginResponse;
import com.solarlab.adboard.dto.response.user.UserResponseRegistration;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private KeycloakProperties keycloakProperties;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldRejectExistingEmail() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(User.builder().build()));

        assertThrows(IllegalArgumentException.class, () ->
                authService.registerUser(new UserRequestRegistration("User", "user@test.com", "+123", "pass"))
        );
    }

    @Test
    void registerShouldCreateUserAndReturnResponse() {
        UserRequestRegistration request = new UserRequestRegistration("User", "user@test.com", "+123", "pass");
        User savedUser = User.builder()
                .id(1L).email("user@test.com")
                .name("User")
                .phone("+123")
                .build();
        UserResponseRegistration response = UserResponseRegistration.builder()
                .id(1L)
                .email("user@test.com")
                .build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(keycloakAdminService.createUser(any())).thenReturn("kc-1");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponseRegistration(savedUser)).thenReturn(response);

        UserResponseRegistration result = authService.registerUser(request);

        assertEquals(response, result);
        verify(keycloakAdminService).assignClientRoleToUser("kc-1", "USER");
        verify(applicationEventPublisher).publishEvent(new UserRegisteredEvent(1L, "User", "user@test.com"));
    }

    @Test
    void registerShouldRollbackKeycloakWhenLocalSaveFails() {
        UserRequestRegistration request = new UserRequestRegistration("User", "user@test.com", "+123", "pass");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(keycloakAdminService.createUser(any())).thenReturn("kc-1");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("db fail"));

        assertThrows(IllegalArgumentException.class, () -> authService.registerUser(request));

        verify(keycloakAdminService).deleteUserById("kc-1");
    }

    @Test
    void loginShouldReturnBodyOnSuccess() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn("secret");
        when(keycloakProperties.tokenUrl()).thenReturn("http://token");
        LoginResponse loginResponse = new LoginResponse("access", "refresh", 10L, 10L, "Bearer");
        when(restTemplate.postForEntity(eq("http://token"), any(), eq(LoginResponse.class)))
                .thenReturn(ResponseEntity.ok(loginResponse));

        LoginResponse result = authService.login(new LoginRequest("user@test.com", "pass"));

        assertEquals(loginResponse, result);
    }

    @Test
    void loginShouldThrowWhenResponseBodyEmpty() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn(null);
        when(keycloakProperties.tokenUrl()).thenReturn("http://token");
        when(restTemplate.postForEntity(eq("http://token"), any(), eq(LoginResponse.class)))
                .thenReturn(ResponseEntity.ok(new LoginResponse(null, null, 10L, 10L, "Bearer")));

        assertThrows(IllegalStateException.class, () ->
                authService.login(new LoginRequest("user@test.com", "pass"))
        );
    }

    @Test
    void loginShouldMapBadCredentialsToIllegalArgument() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn(null);
        when(keycloakProperties.tokenUrl()).thenReturn("http://token");
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", null, new byte[0], StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(eq("http://token"), any(), eq(LoginResponse.class)))
                .thenThrow(exception);

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(new LoginRequest("user@test.com", "pass"))
        );
    }

    @Test
    void refreshTokenShouldReturnBodyOnSuccess() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn("secret");
        when(keycloakProperties.tokenUrl()).thenReturn("http://token");
        LoginResponse loginResponse = new LoginResponse("access-2", "refresh-2", 10L, 10L, "Bearer");
        when(restTemplate.postForEntity(eq("http://token"), any(), eq(LoginResponse.class)))
                .thenReturn(ResponseEntity.ok(loginResponse));

        LoginResponse result = authService.refreshToken(new RefreshTokenRequest("refresh-1"));

        assertEquals(loginResponse, result);
    }

    @Test
    void refreshTokenShouldMapInvalidTokenToIllegalArgument() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn(null);
        when(keycloakProperties.tokenUrl()).thenReturn("http://token");
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, new byte[0], StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(eq("http://token"), any(), eq(LoginResponse.class)))
                .thenThrow(exception);

        assertThrows(IllegalArgumentException.class, () ->
                authService.refreshToken(new RefreshTokenRequest("refresh-1"))
        );
    }

    @Test
    void logoutShouldCallKeycloakOnSuccess() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn("secret");
        when(keycloakProperties.logoutUrl()).thenReturn("http://logout");
        when(restTemplate.postForEntity(eq("http://logout"), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.noContent().build());

        authService.logout(new LogoutRequest("refresh-1"));

        verify(restTemplate).postForEntity(eq("http://logout"), any(), eq(Void.class));
    }

    @Test
    void logoutShouldMapInvalidTokenToIllegalArgument() {
        when(keycloakProperties.clientId()).thenReturn("client");
        when(keycloakProperties.clientSecret()).thenReturn(null);
        when(keycloakProperties.logoutUrl()).thenReturn("http://logout");
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, new byte[0], StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(eq("http://logout"), any(), eq(Void.class)))
                .thenThrow(exception);

        assertThrows(IllegalArgumentException.class, () ->
                authService.logout(new LogoutRequest("refresh-1"))
        );
    }
}
