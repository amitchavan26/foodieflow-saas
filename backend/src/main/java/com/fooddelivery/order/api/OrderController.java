package com.fooddelivery.order.api;

import com.fooddelivery.common.security.CurrentUserService;
import com.fooddelivery.order.api.dto.CreateOrderRequest;
import com.fooddelivery.order.application.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateOrderRequest request) {
        
        UUID customerUserId = currentUserService.getCurrentUserId();
        long totalAmountInCents = 1500L; // Example: ₹15.00 (Replace with actual cart total logic)
        
        Map<String, Object> paymentDetails = orderService.createOrder(restaurantId, customerUserId, request.deliveryAddress(), totalAmountInCents);
        
        return ResponseEntity.ok(paymentDetails);
    }
}