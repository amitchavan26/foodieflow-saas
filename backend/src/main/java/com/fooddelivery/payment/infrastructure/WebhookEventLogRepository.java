package com.fooddelivery.payment.infrastructure;

import com.fooddelivery.payment.domain.WebhookEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WebhookEventLogRepository extends JpaRepository<WebhookEventLog, java.util.UUID> {
    Optional<WebhookEventLog> findByEventId(String eventId);
}