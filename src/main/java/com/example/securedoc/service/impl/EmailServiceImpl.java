package com.example.securedoc.service.impl;

import com.example.securedoc.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.example.securedoc.utils.EmailUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String PASSWORD_RESET_REQUEST = "Password Reset Request";
    private final JavaMailSender mailSender;

    @Value("${spring.mail.verify.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountEmail(String name, String email, String token) {
        try {
            var message = createMessage(NEW_USER_ACCOUNT_VERIFICATION, fromEmail, email, getEmailMessage(name, host, token));
            mailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    @Async
    public void sendResetPasswordEmail(String name, String email, String token) {
        try {
            var message = createMessage(PASSWORD_RESET_REQUEST, fromEmail, email, getResetPasswordMessage(name, host, token));
            mailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private SimpleMailMessage createMessage(String subject, String from, String to, String text) {
        var message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setFrom(from);
        message.setTo(to);
        message.setText(text);

        return message;
    }
}
