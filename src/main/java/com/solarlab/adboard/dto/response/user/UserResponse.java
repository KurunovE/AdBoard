package com.solarlab.adboard.dto.response.user;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String name,
        String email,
        String phone
) {}
