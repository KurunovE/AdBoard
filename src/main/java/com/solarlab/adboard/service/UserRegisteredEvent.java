package com.solarlab.adboard.service;

public record UserRegisteredEvent(
        Long userId,
        String name,
        String email
) {}
