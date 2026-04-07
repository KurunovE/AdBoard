package com.solarlab.adboard.dto.response.comment;

import com.solarlab.adboard.dto.response.user.UserResponse;
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
