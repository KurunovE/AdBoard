package com.solarlab.adboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record YandexPublicUrlResponse(
        @JsonProperty("public_url") String publicUrl
) {}
