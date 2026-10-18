package com.fooddelivery.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING_ACCEPTANCE,
    ACCEPTED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}