package com.solarlab.adboard.dto.response.user;

import lombok.Builder;

@Builder
public record UserAdvertisementResponse(
        Long id,
        String name,
        String phone
) {
}
