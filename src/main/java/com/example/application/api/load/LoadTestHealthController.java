package com.example.application.api.load;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /api/load/health – einfacher Ping für JMeter-Warmup
 */
@RestController
@RequestMapping("/api/load")
public class LoadTestHealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "logistik");
    }
}
