package com.solarlab.adboard.dto.response.image;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ImageResponse(
        Long id,
        String url,
        int sortOrder,
        LocalDateTime uploadedAt
) {
}
