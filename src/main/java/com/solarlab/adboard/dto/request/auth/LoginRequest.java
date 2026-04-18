package com.solarlab.adboard.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password
) {}
