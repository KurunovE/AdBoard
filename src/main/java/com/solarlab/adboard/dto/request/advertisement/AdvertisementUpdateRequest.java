package com.solarlab.adboard.dto.request.advertisement;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record AdvertisementUpdateRequest(
        String title,
        String description,
        @DecimalMin("0.0")
        BigDecimal price,
        Long categoryId
) {
}
