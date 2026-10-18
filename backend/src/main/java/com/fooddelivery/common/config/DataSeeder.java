package com.fooddelivery.common.config;

import com.fooddelivery.user.domain.User;
import com.fooddelivery.user.domain.UserStatus;
import com.fooddelivery.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding database with test users...");
            
            User customer = new User();
            customer.setEmail("customer@test.com");
            customer.setPasswordHash(passwordEncoder.encode("password123"));
            customer.setFullName("Test Customer");
            customer.setRole(User.UserRole.CUSTOMER);
            customer.setStatus(UserStatus.ACTIVE);
            userRepository.save(customer);

            User owner = new User();
            owner.setEmail("owner@test.com");
            owner.setPasswordHash(passwordEncoder.encode("password123"));
            owner.setFullName("Test Owner");
            owner.setRole(User.UserRole.RESTAURANT_OWNER);
            owner.setStatus(UserStatus.ACTIVE);
            userRepository.save(owner);

            log.info("Test users created: customer@test.com / password123");
        }
    }
}