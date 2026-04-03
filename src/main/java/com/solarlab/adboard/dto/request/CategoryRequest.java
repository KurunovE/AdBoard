package com.solarlab.adboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name cannot be null")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,
        Long parentId
) {}
