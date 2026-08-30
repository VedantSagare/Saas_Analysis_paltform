package com.example.saasanalytics.controller;

import com.example.saasanalytics.domain.EventIngestion;
import com.example.saasanalytics.service.EventIngestionService;
import com.example.saasanalytics.web.dto.EventIngestionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventIngestionService eventIngestionService;

    public EventController(EventIngestionService eventIngestionService) {
        this.eventIngestionService = eventIngestionService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> ingest(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody EventIngestionRequest request) {
        try {
            EventIngestion saved = eventIngestionService.ingest(apiKey, request);
            return ResponseEntity.accepted().body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }
}
