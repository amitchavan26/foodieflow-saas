package com.fooddelivery.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "Delivery address is required")
    String deliveryAddress,

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    List<OrderItemRequest> items
) {
    public record OrderItemRequest(
        @NotNull(message = "Menu item ID is required")
        java.util.UUID menuItemId,

        @NotNull(message = "Quantity is required")
        @jakarta.validation.constraints.Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
    ) {}
}