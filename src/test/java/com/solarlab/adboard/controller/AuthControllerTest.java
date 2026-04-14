package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.auth.LogoutRequest;
import com.solarlab.adboard.dto.request.auth.RefreshTokenRequest;
import com.solarlab.adboard.dto.request.user.UserRequestRegistration;
import com.solarlab.adboard.dto.response.auth.LoginResponse;
import com.solarlab.adboard.dto.response.user.UserResponseRegistration;
import com.solarlab.adboard.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginShouldDelegate() {
        LoginRequest loginRequest = new LoginRequest("user@test.com", "pass");
        LoginResponse loginResponse = new LoginResponse(
                "access", "refresh", 1L, 1L, "Bearer"
        );
        when(authService.login(loginRequest)).thenReturn(loginResponse);

        assertEquals(loginResponse, authController.login(loginRequest).getBody());
    }

    @Test
    void refreshShouldDelegate() {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh");
        LoginResponse loginResponse = new LoginResponse(
                "access-2", "refresh-2", 1L, 1L, "Bearer"
        );
        when(authService.refreshToken(refreshTokenRequest)).thenReturn(loginResponse);

        assertEquals(loginResponse, authController.refresh(refreshTokenRequest).getBody());
    }

    @Test
    void logoutShouldDelegate() {
        LogoutRequest logoutRequest = new LogoutRequest("refresh");

        var response = authController.logout(logoutRequest);

        verify(authService).logout(logoutRequest);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void registerShouldDelegate() {
        UserRequestRegistration registration = new UserRequestRegistration(
                "User", "user@test.com", "+123", "pass"
        );
        UserResponseRegistration response = UserResponseRegistration.builder()
                .id(1L)
                .email("user@test.com")
                .build();
        when(authService.registerUser(registration)).thenReturn(response);

        assertEquals(response, authController.register(registration).getBody());
    }
}
