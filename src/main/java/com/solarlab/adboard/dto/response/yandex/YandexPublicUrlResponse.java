package com.solarlab.adboard.dto.response.yandex;

import com.fasterxml.jackson.annotation.JsonProperty;

public record YandexPublicUrlResponse(
        @JsonProperty("public_url") String publicUrl
) {}
