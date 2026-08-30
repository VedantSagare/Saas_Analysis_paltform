package com.example.saasanalytics.service;

import com.example.saasanalytics.config.TenantContext;
import com.example.saasanalytics.domain.ApiKey;
import com.example.saasanalytics.domain.EventDeadLetter;
import com.example.saasanalytics.domain.EventIngestion;
import com.example.saasanalytics.repository.EventDeadLetterRepository;
import com.example.saasanalytics.repository.EventIngestionRepository;
import com.example.saasanalytics.web.dto.EventIngestionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventIngestionService {

    private final ApiKeyService apiKeyService;
    private final EventIngestionRepository eventIngestionRepository;
    private final EventDeadLetterRepository eventDeadLetterRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsTopic;

    public EventIngestionService(
            ApiKeyService apiKeyService,
            EventIngestionRepository eventIngestionRepository,
            EventDeadLetterRepository eventDeadLetterRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.events.topic:events}") String eventsTopic) {
        this.apiKeyService = apiKeyService;
        this.eventIngestionRepository = eventIngestionRepository;
        this.eventDeadLetterRepository = eventDeadLetterRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.eventsTopic = eventsTopic;
    }

    public EventIngestion ingest(String apiKeyValue, EventIngestionRequest request) {
        ApiKey apiKey = apiKeyService.validateApiKey(apiKeyValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid API key for this tenant"));

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is missing");
        }

        if (eventIngestionRepository.findByTenantIdAndEventId(tenantId, request.getEventId()).isPresent()) {
            return eventIngestionRepository.findByTenantIdAndEventId(tenantId, request.getEventId()).get();
        }

        try {
            String payloadJson = objectMapper.writeValueAsString(request.getPayload());
            EventIngestion event = new EventIngestion();
            event.setEventId(request.getEventId());
            event.setEventType(request.getEventType());
            event.setPayload(payloadJson);
            event.setTenant(apiKey.getTenant());
            event.setStatus("QUEUED");

            EventIngestion saved = eventIngestionRepository.save(event);
            kafkaTemplate.send(eventsTopic, request.getEventId(), payloadJson);
            return saved;
        } catch (JsonProcessingException e) {
            EventDeadLetter deadLetter = new EventDeadLetter();
            deadLetter.setEventId(request.getEventId());
            deadLetter.setEventType(request.getEventType());
            deadLetter.setPayload(String.valueOf(request.getPayload()));
            deadLetter.setError("JSON serialization failed: " + e.getMessage());
            deadLetter.setTenant(apiKey.getTenant());
            eventDeadLetterRepository.save(deadLetter);
            throw new IllegalArgumentException("Invalid event payload", e);
        }
    }
}
