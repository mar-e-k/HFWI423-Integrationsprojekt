package com.example.application.api.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Service-Status")
public class HealthController {

    @Operation(summary = "Service-Status pruefen")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "logistik");
    }
}
