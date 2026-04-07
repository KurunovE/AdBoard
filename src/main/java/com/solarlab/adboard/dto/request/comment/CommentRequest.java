package com.solarlab.adboard.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment text cannot be empty")
        @Size(max = 2000, message = "Comment text must not exceed 2000 characters")
        String text
) {
}
