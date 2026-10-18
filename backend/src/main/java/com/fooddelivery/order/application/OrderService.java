package com.fooddelivery.order.application;

import com.fooddelivery.order.api.dto.CreateOrderRequest;
import com.fooddelivery.order.api.dto.OrderResponse;
import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.domain.OrderCreatedEvent; // <-- THIS IMPORT IS CRUCIAL
import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.infrastructure.OrderRepository;
import com.fooddelivery.restaurant.domain.MenuItem;
import com.fooddelivery.restaurant.infrastructure.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final ApplicationEventPublisher eventPublisher; // <-- ADDED THIS

    @Transactional
    public OrderResponse createOrder(UUID restaurantId, UUID customerUserId, CreateOrderRequest request) {
        long totalAmountInCents = 0;
        
        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.menuItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found: " + itemReq.menuItemId()));
            
            if (!menuItem.getRestaurantId().equals(restaurantId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to this restaurant");
            }
            if (!menuItem.isAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item is not available: " + menuItem.getName());
            }

            totalAmountInCents += (menuItem.getPriceInCents() * itemReq.quantity());
        }

        Order order = new Order();
        order.setRestaurantId(restaurantId);
        order.setCustomerUserId(customerUserId);
        order.setDeliveryAddress(request.deliveryAddress());
        order.setTotalAmountInCents(totalAmountInCents);
        order.setStatus(OrderStatus.PENDING_ACCEPTANCE);

        Order savedOrder = orderRepository.save(order);
        
        // Publish the event for background processing (Layer 5c)
        eventPublisher.publishEvent(new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getRestaurantId(),
            savedOrder.getCustomerUserId(),
            savedOrder.getTotalAmountInCents()
        ));
        
        return OrderResponse.fromEntity(savedOrder);
    }
}