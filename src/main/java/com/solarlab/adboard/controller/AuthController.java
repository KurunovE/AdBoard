package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.LoginRequest;
import com.solarlab.adboard.dto.request.UserRequestRegistration;
import com.solarlab.adboard.dto.response.LoginResponse;
import com.solarlab.adboard.dto.response.UserResponseRegistration;
import com.solarlab.adboard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseRegistration> register(
            @Valid @RequestBody UserRequestRegistration userRequestRegistration
    ) {
        return ResponseEntity.ok(authService.registerUser(userRequestRegistration));
    }
}
