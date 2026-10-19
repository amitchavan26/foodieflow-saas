package com.fooddelivery.order.application;

import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.domain.OrderCreatedEvent;
import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.infrastructure.OrderRepository;
import com.fooddelivery.restaurant.infrastructure.RestaurantRepository;
import com.fooddelivery.user.infrastructure.UserRepository;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Transactional
    public Map<String, Object> createOrder(UUID restaurantId, UUID customerUserId, String deliveryAddress, long totalAmountInCents) {
        // 1. Create the Order in PENDING_PAYMENT status
        Order order = new Order();
        order.setRestaurantId(restaurantId);
        order.setCustomerUserId(customerUserId);
        order.setDeliveryAddress(deliveryAddress);
        order.setTotalAmountInCents(totalAmountInCents);
        order.setStatus(OrderStatus.PENDING_PAYMENT); 
        
        Order savedOrder = orderRepository.save(order);

        // 2. Create a Razorpay Order
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", totalAmountInCents); // Amount in paise (cents)
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + savedOrder.getId().toString().substring(0, 8));
            orderRequest.put("notes", new JSONObject().put("order_id", savedOrder.getId().toString()));

            // Use fully qualified name to avoid conflict with com.fooddelivery.order.domain.Order
            com.razorpay.Order razorpayOrder = client.orders.create(orderRequest);
            
            // 3. Publish event for background processing
            eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId(), restaurantId, customerUserId, totalAmountInCents));

            // 4. Return details needed by the frontend
            return Map.of(
                "razorpayOrderId", razorpayOrder.get("id"),
                "amount", totalAmountInCents,
                "currency", "INR",
                "keyId", razorpayKeyId,
                "name", "FoodieFlow",
                "description", "Order Payment"
            );

        } catch (Exception e) {
            log.error("Failed to create Razorpay Order", e);
            throw new RuntimeException("Payment initialization failed: " + e.getMessage(), e);
        }
    }
}