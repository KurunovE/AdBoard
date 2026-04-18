package com.solarlab.adboard.dto.request.user;

import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank")
        String name,
        @Pattern(regexp = ".*\\S.*", message = "Phone cannot be blank")
        String phone
) {}
