package com.solarlab.adboard.service;

public interface EmailService {
    void sendWelcomeEmail(UserRegisteredEvent event);
}
