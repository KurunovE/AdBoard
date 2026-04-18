package com.solarlab.adboard.dto.request.category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "Category name cannot be blank")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,
        Long parentId
) {}
