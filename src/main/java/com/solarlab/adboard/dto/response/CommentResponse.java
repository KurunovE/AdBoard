package com.solarlab.adboard.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CommentResponse(
        Long id,
        String text,
        UserResponse author,
        Long advertisementId,
        LocalDateTime createdAt
) {
}
