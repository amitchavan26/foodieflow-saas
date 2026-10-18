package com.fooddelivery.common.security;

import com.fooddelivery.order.infrastructure.OrderRepository;
import com.fooddelivery.restaurant.infrastructure.RestaurantRepository;
import com.fooddelivery.user.infrastructure.UserRepository;
import com.fooddelivery.order.repository.OrderRepository;
import com.fooddelivery.restaurant.domain.Restaurant;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantIsolationAspect {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    @Around("@annotation(com.fooddelivery.common.security.RequireTenantOwnership)")
    public Object checkTenantOwnership(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(FORBIDDEN, "Unauthenticated");
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Extract restaurantId from method arguments (assumes it's passed as a UUID or String)
        UUID requestedRestaurantId = null;
        UUID requestedOrderId = null;

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof UUID uuid) {
                // Simple heuristic: if the method is about orders, it's an orderId, else restaurantId
                // In a real app, you'd use @Annotation parameters, but this works for V1
                if (joinPoint.getSignature().getName().contains("order")) {
                    requestedOrderId = uuid;
                } else {
                    requestedRestaurantId = uuid;
                }
            }
        }

        // 1. Admins bypass tenant checks
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return joinPoint.proceed();
        }

        // 2. Restaurant Owner Check
        if (currentUser.getRole() == User.UserRole.RESTAURANT_OWNER) {
            if (requestedRestaurantId != null) {
                Restaurant restaurant = restaurantRepository.findById(requestedRestaurantId)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Restaurant not found"));
                if (!restaurant.getOwnerUserId().equals(currentUser.getId())) {
                    throw new ResponseStatusException(NOT_FOUND, "Resource not found"); // 404 prevents enumeration
                }
            }
            if (requestedOrderId != null) {
                Order order = orderRepository.findById(requestedOrderId)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
                if (!order.getRestaurantId().equals(requestedRestaurantId)) { // Ensure it matches the path var
                    throw new ResponseStatusException(NOT_FOUND, "Resource not found");
                }
            }
        }

        // 3. Customer Check (Can only see their own orders)
        if (currentUser.getRole() == User.UserRole.CUSTOMER && requestedOrderId != null) {
            Order order = orderRepository.findById(requestedOrderId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
            if (!order.getCustomerUserId().equals(currentUser.getId())) {
                throw new ResponseStatusException(NOT_FOUND, "Resource not found");
            }
        }

        return joinPoint.proceed();
    }
}