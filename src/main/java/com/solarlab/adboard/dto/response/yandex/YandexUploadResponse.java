package com.solarlab.adboard.dto.response.yandex;

import com.fasterxml.jackson.annotation.JsonProperty;

public record YandexUploadResponse(
        @JsonProperty("href") String href,
        @JsonProperty("method") String method,
        @JsonProperty("templated") boolean templated
) {}
