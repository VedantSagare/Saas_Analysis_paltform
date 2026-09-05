package com.example.saasanalytics.controller;

import com.example.saasanalytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("analytics-processing-active");
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<AnalyticsService.AnalyticsSummary> summary(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        Instant fromInstant = from == null || from.isBlank() ? Instant.now().minusSeconds(7L * 24 * 60 * 60) : Instant.parse(from);
        Instant toInstant = to == null || to.isBlank() ? Instant.now() : Instant.parse(to);
        return ResponseEntity.ok(analyticsService.getSummary(tenantId, fromInstant, toInstant));
    }

    @GetMapping("/analytics/activity")
    public ResponseEntity<List<AnalyticsService.ActivityPoint>> activity(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromInstant = from == null || from.isBlank() ? Instant.now().minusSeconds(7L * 24 * 60 * 60) : Instant.parse(from);
        Instant toInstant = to == null || to.isBlank() ? Instant.now() : Instant.parse(to);
        return ResponseEntity.ok(analyticsService.getDailyActivity(tenantId, fromInstant, toInstant));
    }
}
