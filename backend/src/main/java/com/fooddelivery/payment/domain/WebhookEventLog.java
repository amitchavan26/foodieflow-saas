package com.fooddelivery.payment.domain;

import com.fooddelivery.common.persistence.BaseAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_event_logs")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEventLog extends BaseAuditedEntity {
    
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId; // e.g., Stripe Event ID

    @Column(name = "provider", nullable = false, length = 50)
    private String provider; // e.g., "STRIPE", "RAZORPAY"

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // e.g., "payment_intent.succeeded"

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
}