package com.example.smtp.worker;

import com.example.common.entity.OutboundQueue;
import com.example.common.repository.MailQueueRepository;
import com.example.smtp.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueWorker {

    private final MailQueueRepository mailQueueRepository;
    private final DeliveryService deliveryService;

    @org.springframework.beans.factory.annotation.Value("${queue.worker.batch.size:10}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${queue.worker.fixed.delay:10000}") // Run every 10 seconds
    public void processQueue() {
        log.debug("Checking for outbound emails...");
        List<OutboundQueue> pendingItems = mailQueueRepository.findByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                "PENDING", LocalDateTime.now(), PageRequest.of(0, batchSize));

        if (pendingItems.isEmpty()) {
            // Also check for RETRY items that are due
            pendingItems = mailQueueRepository.findByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                    "RETRY", LocalDateTime.now(), PageRequest.of(0, batchSize));
        }

        if (!pendingItems.isEmpty()) {
            log.info("Found {} items to process", pendingItems.size());
            for (OutboundQueue item : pendingItems) {
                try {
                    deliveryService.processItem(item);
                } catch (Exception e) {
                    log.error("Error processing item ID: {}", item.getId(), e);
                }
            }
        }
    }
}
