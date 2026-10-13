package com.nexarats.academy.common.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Foundation health endpoint for Nexarats Academy Core Backend.
 * Provides safe service status confirmation without exposing environmental secrets.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    private static final String SERVICE_NAME = "nexarats-academy-core";
    private static final String VERSION = "0.0.1-SNAPSHOT";

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", SERVICE_NAME,
                "version", VERSION,
                "timestamp", Instant.now().toString()
        ));
    }
}
