package com.solarlab.adboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "mail")
public record MailProperties(
        String host,
        Integer port,
        String username,
        String password,
        String from,
        Map<String, String> properties
) {}
