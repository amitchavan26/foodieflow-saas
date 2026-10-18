package com.fooddelivery.payment.api;

import com.fooddelivery.payment.domain.WebhookEventLog;
import com.fooddelivery.payment.infrastructure.WebhookEventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final WebhookEventLogRepository webhookEventLogRepository;

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentWebhook(
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload) {
        
        String eventId = (String) payload.get("id");
        String eventType = (String) payload.get("type");

        if (eventId == null) {
            log.warn("Webhook received without an event ID");
            return ResponseEntity.badRequest().body("Missing event ID");
        }

        if (webhookEventLogRepository.findByEventId(eventId).isPresent()) {
            log.info("Idempotency check passed: Webhook event {} already processed.", eventId);
            return ResponseEntity.ok("Event already processed");
        }

        try {
            log.info("Processing new webhook event: {} of type {}", eventId, eventType);
            
            WebhookEventLog logEntry = new WebhookEventLog();
            logEntry.setEventId(eventId);
            logEntry.setProvider("STRIPE_MOCK");
            logEntry.setEventType(eventType);
            logEntry.setPayload(payload.toString());
            
            webhookEventLogRepository.save(logEntry);
            
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", eventId, e);
            return ResponseEntity.internalServerError().body("Processing failed");
        }
    }
}