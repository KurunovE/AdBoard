package com.solarlab.adboard.service.impl;

import com.solarlab.adboard.config.MailProperties;
import com.solarlab.adboard.service.EmailService;
import com.solarlab.adboard.service.UserRegisteredEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class YandexEmailService implements EmailService {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final TemplateEngine templateEngine;

    @Override
    @Async("mailTaskExecutor")
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            Context context = new Context();
            context.setVariable("name", event.name());

            helper.setFrom(mailProperties.from());
            helper.setTo(event.email());
            helper.setSubject("Welcome to AdBoard");
            helper.setText(templateEngine.process("mail/welcome-email", context), true);

            javaMailSender.send(message);
            log.info("Sent welcome email to userId={} email={}", event.userId(), event.email());
        } catch (MessagingException ex) {
            log.error("Failed to prepare welcome email to userId={} email={}",
                    event.userId(), event.email(), ex);
        } catch (MailException ex) {
            log.error("Failed to send welcome email to userId={} email={}",
                    event.userId(), event.email(), ex);
        }
    }
}
