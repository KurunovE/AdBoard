package com.solarlab.adboard.dto.request.advertisement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AdvertisementUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "Title cannot be blank")
        String title,
        String description,
        @DecimalMin("1.0")
        BigDecimal price,
        Long categoryId
) {
}
