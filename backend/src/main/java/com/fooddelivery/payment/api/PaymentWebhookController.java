package com.fooddelivery.payment.api;

import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.infrastructure.OrderRepository;
import com.fooddelivery.payment.domain.WebhookEventLog;
import com.fooddelivery.payment.infrastructure.WebhookEventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/razorpay")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final WebhookEventLogRepository webhookEventLogRepository;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @PostMapping
    @Transactional
    public ResponseEntity<String> handleRazorpayWebhook(@RequestBody String payload) {
        // NOTE: In a real production app, you would verify the Razorpay webhook signature here.
        // For this development phase, we will log the event and return success.
        
        log.info("Received Razorpay webhook payload: {}", payload);
        
        // TODO: Parse JSON payload, check for "event": "payment.captured"
        // Extract order_id from payload.notes.order_id
        // Update order status to PAID
        // Save to WebhookEventLog for idempotency
        
        return ResponseEntity.ok("Webhook received");
    }
}