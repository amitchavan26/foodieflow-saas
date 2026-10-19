package com.fooddelivery.common.security;

import com.fooddelivery.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public UUID getCurrentUserId() {
        // TEMPORARY HARDCODE FOR TESTING STRIPE FLOW
        // We will fix the real JWT extraction in the next step!
        log.warn("USING HARDCODED CUSTOMER ID FOR TESTING. Real auth will be restored later.");
        
        return userRepository.findByEmail("customer@test.com")
            .map(user -> user.getId())
            .orElseThrow(() -> new IllegalStateException("Hardcoded test user 'customer@test.com' not found in database. Did the seeder run?"));
    }
}