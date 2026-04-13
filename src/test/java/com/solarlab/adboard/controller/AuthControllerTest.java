package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.auth.LoginRequest;
import com.solarlab.adboard.dto.request.user.UserRequestRegistration;
import com.solarlab.adboard.dto.response.auth.LoginResponse;
import com.solarlab.adboard.dto.response.user.UserResponseRegistration;
import com.solarlab.adboard.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
