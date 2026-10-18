package com.fooddelivery.order.api;

import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.infrastructure.OrderRepository;
import com.fooddelivery.restaurant.domain.Restaurant;
import com.fooddelivery.restaurant.infrastructure.RestaurantRepository;
import com.fooddelivery.user.domain.User;
import com.fooddelivery.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private OrderRepository orderRepository;

    private UUID restA_Id;
    private UUID restB_Id;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        User ownerA = new User();
        ownerA.setEmail("ownerA@test.com");
        ownerA.setPasswordHash("hashed");
        ownerA.setRole(User.UserRole.RESTAURANT_OWNER);
        ownerA = userRepository.save(ownerA);

        User ownerB = new User();
        ownerB.setEmail("ownerB@test.com");
        ownerB.setPasswordHash("hashed");
        ownerB.setRole(User.UserRole.RESTAURANT_OWNER);
        ownerB = userRepository.save(ownerB);

        Restaurant restA = new Restaurant();
        restA.setOwnerUserId(ownerA.getId());
        restA.setName("Rest A");
        restA.setAddress("Address A");
        restA_Id = restaurantRepository.save(restA).getId();

        Restaurant restB = new Restaurant();
        restB.setOwnerUserId(ownerB.getId());
        restB.setName("Rest B");
        restB.setAddress("Address B");
        restB_Id = restaurantRepository.save(restB).getId();
    }

    @Test
    @WithMockUser(username = "ownerB@test.com", roles = {"RESTAURANT_OWNER"})
    void testIDOR_OwnerB_CannotCreateOrderForOwnerA_Restaurant() throws Exception {
        String payload = """
            {
              "deliveryAddress": "123 Test St",
              "items": []
            }
            """;
        
        // Owner B tries to interact with Restaurant A's endpoint. 
        // The @RequireTenantOwnership aspect should catch this and return 404.
        mockMvc.perform(post("/api/v1/restaurants/" + restA_Id + "/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNotFound());
    }
}