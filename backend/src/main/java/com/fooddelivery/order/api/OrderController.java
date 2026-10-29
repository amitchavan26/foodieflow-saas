package com.fooddelivery.order.api;

import com.fooddelivery.order.application.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fooddelivery.common.security.JwtService; // Adjust import if needed

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderTrackingService trackingService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // ... (keep your existing createOrder method here) ...

    @GetMapping(value = "/{orderId}/track", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter trackOrder(@PathVariable UUID orderId, @RequestParam("token") String token) {
        log.info("🚀 SSE Track endpoint hit for order: {}", orderId);
        
        try {
            // 1. Manually validate the token (bypasses GlobalExceptionHandler JSON issues)
            String userEmail = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            if (userEmail == null || !jwtService.isTokenValid(token, userDetails)) {
                throw new RuntimeException("Invalid or expired token");
            }

            // 2. Start tracking
            SseEmitter emitter = trackingService.createEmitter(orderId);
            trackingService.simulateRestaurantWorkflow(orderId);
            return emitter;
            
        } catch (Exception e) {
            log.error("💥 Tracking failed for {}: {}", orderId, e.getMessage());
            
            // 3. Safely return an SSE error event instead of throwing
            SseEmitter errorEmitter = new SseEmitter(0L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("Tracking failed: " + e.getMessage()));
                errorEmitter.completeWithError(e);
            } catch (IOException ioException) {
                log.error("Failed to send SSE error event", ioException);
            }
            return errorEmitter;
        }
    }
}