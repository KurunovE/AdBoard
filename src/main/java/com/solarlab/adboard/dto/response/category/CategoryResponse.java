package com.solarlab.adboard.dto.response.category;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId,
        String parentName
) {}
