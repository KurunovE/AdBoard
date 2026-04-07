package com.solarlab.adboard.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ExceptionResponse(
        String message,
        int status,
        LocalDateTime timestamp
) {}
