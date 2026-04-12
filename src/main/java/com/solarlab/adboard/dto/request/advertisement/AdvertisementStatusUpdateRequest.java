package com.solarlab.adboard.dto.request.advertisement;

import com.solarlab.adboard.enums.AdvertisementStatus;
import jakarta.validation.constraints.NotNull;

public record AdvertisementStatusUpdateRequest(
        @NotNull
        AdvertisementStatus status
) {
}
