package com.solarlab.adboard.dto.request.advertisement;

import java.math.BigDecimal;

public record AdvertisementFilter(
        Long categoryId,
        Long authorId,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
