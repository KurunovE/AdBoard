package com.solarlab.adboard.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId,
        String parentName
) {}
