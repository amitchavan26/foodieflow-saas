package com.fooddelivery.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PENDING_ACCEPTANCE,
    ACCEPTED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}