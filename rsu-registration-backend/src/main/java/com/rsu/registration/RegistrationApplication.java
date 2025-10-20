package com.rsu.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for RSU Student Registration System
 * Implements Enterprise Integration Patterns (EIP) for processing student registrations
 * through asynchronous message queues
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.rsu.registration")
public class RegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistrationApplication.class, args);
    }
}
