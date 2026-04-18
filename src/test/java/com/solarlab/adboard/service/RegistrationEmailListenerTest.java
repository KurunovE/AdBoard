package com.solarlab.adboard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrationEmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationEmailListener registrationEmailListener;

    @Test
    void onUserRegisteredShouldDelegateToEmailService() {
        UserRegisteredEvent event = new UserRegisteredEvent(1L, "User", "user@test.com");

        registrationEmailListener.onUserRegistered(event);

        verify(emailService).sendWelcomeEmail(event);
    }
}
