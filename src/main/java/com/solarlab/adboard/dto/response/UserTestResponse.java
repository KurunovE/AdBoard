package com.solarlab.adboard.dto.response;

import lombok.Builder;

@Builder
public record UserTestResponse(
        String name,
        String email,
        String phone,
        String password
) {}
