package com.example.securedoc.event.listener;

import com.example.securedoc.entity.UserEntity;
import com.example.securedoc.event.UserEvent;
import com.example.securedoc.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final EmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent event) {
        switch (event.getEventType()) {
            case REGISTRATION -> {
                UserEntity user = event.getUser();
                emailService.sendNewAccountEmail(
                        user.getFirstName(),
                        user.getEmail(),
                        (String) event.getData().get("key")
                );
            }

            case PASSWORD_RESET -> {
                UserEntity user = event.getUser();
                emailService.sendResetPasswordEmail(
                        user.getFirstName(),
                        user.getEmail(),
                        (String) event.getData().get("key")
                );
            }

            default -> {}
        }
    }
}
