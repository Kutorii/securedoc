package com.example.securedoc.domain;

import com.example.securedoc.repository.ConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.example.securedoc.constant.Constants.*;

@Component
@RequiredArgsConstructor
public class ConfirmationConfiguration {
    private final ConfirmationRepository confirmationRepository;

    @Scheduled(fixedRate = CONFIRMATION_SCHEDULED_TASK_RATE)
    public void checkForOutdatedConfirmations() {
        var confirmations = confirmationRepository.findAll();
        confirmations.forEach(confirmation -> {
            if (confirmation.isExpired()) {
                confirmationRepository.delete(confirmation);
            }
        });
    }
}
