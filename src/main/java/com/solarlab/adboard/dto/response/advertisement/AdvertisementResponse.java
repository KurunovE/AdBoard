package com.solarlab.adboard.dto.response.advertisement;

import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.enums.AdvertisementStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdvertisementResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        AdvertisementStatus status,
        Long categoryId,
        String categoryName,
        UserResponse author,
        List<ImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
