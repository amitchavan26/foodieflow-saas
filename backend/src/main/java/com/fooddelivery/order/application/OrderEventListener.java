package com.fooddelivery.order.application;

import com.fooddelivery.order.domain.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("BACKGROUND JOB STARTED: Processing Order Created Event for Order ID: {}", event.orderId());
        
        try {
            // Simulate background work (e.g., payment capture, notifications)
            log.info("BACKGROUND JOB SUCCESS: Order {} processed successfully.", event.orderId());
        } catch (Exception e) {
            log.error("BACKGROUND JOB FAILED: Order {} failed after retries.", event.orderId(), e);
            throw e;
        }
    }
}