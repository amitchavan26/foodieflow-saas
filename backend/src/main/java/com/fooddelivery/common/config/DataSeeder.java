package com.fooddelivery.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) {
        log.info("Database seeding is now handled entirely by Flyway migrations (V2).");
    }
}