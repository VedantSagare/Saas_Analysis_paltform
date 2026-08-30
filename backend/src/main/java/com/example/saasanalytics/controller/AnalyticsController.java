package com.example.saasanalytics.controller;

import com.example.saasanalytics.service.EventProcessingConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final EventProcessingConsumer eventProcessingConsumer;

    public AnalyticsController(EventProcessingConsumer eventProcessingConsumer) {
        this.eventProcessingConsumer = eventProcessingConsumer;
    }

    @GetMapping("/analytics/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("analytics-processing-active");
    }
}
