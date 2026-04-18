package com.solarlab.adboard.service.impl;

import com.solarlab.adboard.config.MailProperties;
import com.solarlab.adboard.service.UserRegisteredEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YandexEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private MailProperties mailProperties;
    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private YandexEmailService yandexEmailService;

    @Test
    void sendWelcomeEmailShouldBuildAndSendHtmlMessage() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        UserRegisteredEvent event = new UserRegisteredEvent(1L, "User", "user@test.com");
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        when(mailProperties.from()).thenReturn("mail@yandex.ru");
        when(templateEngine.process(any(String.class), any())).thenReturn("<html><body>Hello, User!</body></html>");
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        yandexEmailService.sendWelcomeEmail(event);

        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();
        assertEquals("Welcome to AdBoard", sentMessage.getSubject());
        assertEquals("mail@yandex.ru", sentMessage.getFrom()[0].toString());
        assertEquals("user@test.com", sentMessage.getAllRecipients()[0].toString());
        assertTrue(sentMessage.getContent().toString().contains("Hello, User!"));
    }
}
