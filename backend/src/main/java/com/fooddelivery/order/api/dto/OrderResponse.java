package com.fooddelivery.order.api.dto;

import com.fooddelivery.order.domain.Order;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID restaurantId,
    String status,
    Long totalAmountInCents,
    String deliveryAddress
) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getRestaurantId(),
            order.getStatus().name(),
            order.getTotalAmountInCents(),
            order.getDeliveryAddress()
        );
    }
}