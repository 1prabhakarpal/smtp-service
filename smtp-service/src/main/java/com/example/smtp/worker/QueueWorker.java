package com.example.smtp.worker;

import com.example.smtp.entity.OutboundEmail;
import com.example.smtp.repository.OutboundEmailRepository;
import com.example.smtp.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueWorker {

    private final OutboundEmailRepository outboundEmailRepository;
    private final DeliveryService deliveryService;

    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 10000) // Run every 10 seconds
    public void processQueue() {
        List<OutboundEmail> pendingEmails = outboundEmailRepository.findDueForDelivery(LocalDateTime.now());

        if (!pendingEmails.isEmpty()) {
            log.info("Found {} emails due for delivery", pendingEmails.size());
        }

        for (OutboundEmail email : pendingEmails) {
            try {
                deliveryService.deliver(email);
                email.setStatus(OutboundEmail.Status.SENT);
                email.setErrorMessage(null);
                outboundEmailRepository.save(email);
            } catch (Exception e) {
                handleFailure(email, e);
            }
        }
    }

    private void handleFailure(OutboundEmail email, Exception e) {
        int retryCount = email.getRetryCount() + 1;
        email.setRetryCount(retryCount);
        email.setErrorMessage(e.getMessage());

        if (retryCount >= MAX_RETRIES) {
            email.setStatus(OutboundEmail.Status.FAILED);
            log.error("Email ID: {} failed permanently after {} retries", email.getId(), retryCount);
        } else {
            // Exponential backoff: 2^retryCount minutes
            long delayMinutes = (long) Math.pow(2, retryCount);
            email.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            log.warn("Email ID: {} failed. Retrying in {} minutes. Error: {}", email.getId(), delayMinutes,
                    e.getMessage());
        }
        outboundEmailRepository.save(email);
    }
}
