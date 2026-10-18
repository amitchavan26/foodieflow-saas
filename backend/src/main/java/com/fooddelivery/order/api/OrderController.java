package com.fooddelivery.order.api;

import com.fooddelivery.common.security.RequireTenantOwnership;
import com.fooddelivery.common.security.CurrentUserService;
import com.fooddelivery.order.api.dto.CreateOrderRequest;
import com.fooddelivery.order.api.dto.OrderResponse;
import com.fooddelivery.order.application.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService; // Injected securely

    @PostMapping
    @RequireTenantOwnership // Ensures the restaurantId in the path is valid/owned
    public OrderResponse createOrder(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateOrderRequest request) {
        
        // Securely get the actual UUID of the logged-in user from the database
        UUID currentUserId = currentUserService.getCurrentUserId(); 
        
        return orderService.createOrder(restaurantId, currentUserId, request);
    }
}