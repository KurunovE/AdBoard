package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.UserRequestRegistration;
import com.solarlab.adboard.dto.response.UserResponseRegistration;
import com.solarlab.adboard.service.UserService;
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

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseRegistration> register(
            @Valid @RequestBody UserRequestRegistration userRequestRegistration
    ) {
        return ResponseEntity.ok(
                userService.registerUser(userRequestRegistration)
        );
    }
}
