package com.vibe.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Vibe IM Backend.
 *
 * <p>This is a real-time instant messaging backend service built with Spring Boot 3.2.0,
 * providing REST APIs and WebSocket support for real-time communication.</p>
 *
 * @author Vibe IM Team
 */
@SpringBootApplication
public class VibeImApplication {

    /**
     * Main entry point for the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(VibeImApplication.class, args);
    }
}
