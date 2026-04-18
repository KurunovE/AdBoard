package com.solarlab.adboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yandex.disk")
public record YandexDiskProperties(
        String token,
        String apiUrl
) {}
