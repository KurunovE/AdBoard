package com.solarlab.adboard.dto.response.advertisement;

import com.solarlab.adboard.dto.response.user.UserAdvertisementResponse;
import com.solarlab.adboard.enums.AdvertisementStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AdvertisementResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        AdvertisementStatus status,
        Long categoryId,
        String categoryName,
        UserAdvertisementResponse author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
