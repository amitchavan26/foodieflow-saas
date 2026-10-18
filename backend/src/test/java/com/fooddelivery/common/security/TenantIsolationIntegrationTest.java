package com.fooddelivery.common.security;

import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.repository.OrderRepository;
import com.fooddelivery.restaurant.domain.Restaurant;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import com.fooddelivery.user.domain.User;
import com.fooddelivery.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TenantIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UUID restaurantA_Id;
    private UUID restaurantB_Id;
    private UUID orderA_Id;

    @BeforeEach
    void setUp() {
        // Cleanup
        orderRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // Create Users
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

        User customer = new User();
        customer.setEmail("customer@test.com");
        customer.setPasswordHash("hashed");
        customer.setRole(User.UserRole.CUSTOMER);
        customer = userRepository.save(customer);

        // Create Restaurants
        Restaurant restA = new Restaurant();
        restA.setOwnerUserId(ownerA.getId());
        restA.setName("Rest A");
        restA.setAddress("Address A");
        restaurantA_Id = restaurantRepository.save(restA).getId();

        Restaurant restB = new Restaurant();
        restB.setOwnerUserId(ownerB.getId());
        restB.setName("Rest B");
        restB.setAddress("Address B");
        restaurantB_Id = restaurantRepository.save(restB).getId();

        // Create Order for Rest A
        Order orderA = new Order();
        orderA.setCustomerUserId(customer.getId());
        orderA.setRestaurantId(restaurantA_Id);
        orderA.setStatus(OrderStatus.PENDING_ACCEPTANCE);
        orderA.setTotalAmountInCents(1500L);
        orderA.setDeliveryAddress("Test Address");
        orderA_Id = orderRepository.save(orderA).getId();
    }

    @Test
    @WithMockUser(username = "ownerB@test.com", roles = {"RESTAURANT_OWNER"})
    void testIDOR_OwnerB_CannotAccessOwnerA_Order() throws Exception {
        // Owner B tries to access an order belonging to Restaurant A
        // The Aspect should intercept this and return 404 NOT FOUND (not 403)
        mockMvc.perform(get("/api/orders/" + orderA_Id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void testCustomer_CanAccessOwnOrder() throws Exception {
        // This is a mock test structure. In reality, you'd wire the actual controller.
        // This proves the test setup works.
        mockMvc.perform(get("/api/orders/" + orderA_Id))
                .andExpect(status().isOk()); // Assuming controller allows it after aspect passes
    }
}