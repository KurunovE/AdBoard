package com.solarlab.adboard.dto.response;

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
