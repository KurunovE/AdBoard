package com.solarlab.adboard.dto.request.advertisement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdvertisementCreateRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        String description,

        @NotNull(message = "Price cannot be null")
        @DecimalMin("0.0")
        BigDecimal price,

        @NotNull(message = "categoryId cannot be null")
        Long categoryId
) {
}
