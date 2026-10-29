package com.fooddelivery.order.application;

import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTrackingService {

    private final OrderRepository orderRepository;
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(UUID orderId) {
        log.info("Creating SSE emitter for order: {}", orderId);
        SseEmitter emitter = new SseEmitter(3600000L); // 1 hour timeout
        emitters.put(orderId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE emitter completed for order: {}", orderId);
            emitters.remove(orderId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE emitter timed out for order: {}", orderId);
            emitters.remove(orderId);
        });
        emitter.onError((e) -> {
            log.error("SSE emitter error for order: {}", orderId, e);
            emitters.remove(orderId);
        });

        return emitter;
    }

    @Async
    public void simulateRestaurantWorkflow(UUID orderId) {
        log.info("Starting restaurant workflow simulation for order: {}", orderId);
        
        try {
            // Verify order exists first
            if (!orderRepository.existsById(orderId)) {
                log.error("Order {} not found in database!", orderId);
                return;
            }

            log.info("Order {} found. Waiting 5 seconds before PREPARING...", orderId);
            Thread.sleep(5000);
            updateAndEmit(orderId, OrderStatus.PREPARING, "Chef is preparing your delicious food! 👨‍🍳");

            log.info("Waiting 10 seconds before OUT_FOR_DELIVERY...");
            Thread.sleep(10000);
            updateAndEmit(orderId, OrderStatus.OUT_FOR_DELIVERY, "Your order is on the way! 🛵");

            log.info("Waiting 10 seconds before DELIVERED...");
            Thread.sleep(10000);
            updateAndEmit(orderId, OrderStatus.DELIVERED, "Enjoy your meal! 🍕");

            log.info("Workflow simulation completed for order: {}", orderId);

        } catch (InterruptedException e) {
            log.error("Workflow simulation interrupted for order {}", orderId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error in workflow simulation for order {}", orderId, e);
        }
    }

    private void updateAndEmit(UUID orderId, OrderStatus newStatus, String message) {
        log.info("Updating order {} to status: {}", orderId, newStatus);
        
        orderRepository.findById(orderId).ifPresentOrElse(
            order -> {
                order.setStatus(newStatus);
                orderRepository.save(order);
                log.info("Order {} status updated in database", orderId);

                SseEmitter emitter = emitters.get(orderId);
                if (emitter != null) {
                    try {
                        log.info("Sending SSE event to client for order {}", orderId);
                        emitter.send(SseEmitter.event()
                                .name("status-update")
                                .data(Map.of("status", newStatus.name(), "message", message)));
                        log.info("SSE event sent successfully");
                        
                        if (newStatus == OrderStatus.DELIVERED) {
                            log.info("Order delivered, completing SSE emitter");
                            emitter.complete();
                            emitters.remove(orderId);
                        }
                    } catch (IOException e) {
                        log.error("Failed to send SSE event for order {}", orderId, e);
                        emitters.remove(orderId);
                    }
                } else {
                    log.warn("No SSE emitter found for order {}", orderId);
                }
            },
            () -> log.error("Order {} not found when trying to update status", orderId)
        );
    }
}