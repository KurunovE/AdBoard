package com.solarlab.adboard.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserRequestRegistration(
        @NotBlank(message = "Name cannot be null")
        String name,

        @NotBlank(message = "Email cannot be null")
        String email,

        @NotBlank(message = "Phone cannot be null")
        String phone,

        @NotBlank(message = "Password cannot be null")
        String password
) {}
