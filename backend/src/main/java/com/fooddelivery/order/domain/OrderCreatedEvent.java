package com.fooddelivery.order.domain;

import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    UUID restaurantId,
    UUID customerUserId,
    Long totalAmountInCents
) {}