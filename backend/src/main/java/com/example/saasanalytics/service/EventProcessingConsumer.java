package com.example.saasanalytics.service;

import com.example.saasanalytics.domain.EventProcessingMetadata;
import com.example.saasanalytics.repository.EventProcessingMetadataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventProcessingConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingConsumer.class);

    private final EventProcessingMetadataRepository metadataRepository;
    private final ObjectMapper objectMapper;

    public EventProcessingConsumer(EventProcessingMetadataRepository metadataRepository, ObjectMapper objectMapper) {
        this.metadataRepository = metadataRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.events.topic:events}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        List<EventProcessingMetadata> metadataToSave = new ArrayList<>();

        for (ConsumerRecord<String, String> record : records) {
            try {
                JsonNode node = objectMapper.readTree(record.value());
                String eventId = record.key();
                Long tenantId = node.has("tenantId") ? node.get("tenantId").asLong() : 0L;
                String eventType = node.has("eventType") ? node.get("eventType").asText()
                        : node.has("event_type") ? node.get("event_type").asText() : "UNKNOWN";
                String userId = node.has("userId") ? node.get("userId").asText()
                        : node.has("user_id") ? node.get("user_id").asText() : null;
                Long latencyMs = node.has("latencyMs") ? node.get("latencyMs").asLong() :
                        node.has("latency_ms") ? node.get("latency_ms").asLong() : null;
                String status = node.has("status") ? node.get("status").asText() : "PROCESSED";

                EventProcessingMetadata metadata = metadataRepository.findByTenantIdAndEventId(tenantId, eventId)
                        .orElse(new EventProcessingMetadata());

                metadata.setEventId(eventId);
                metadata.setTenantId(tenantId);
                metadata.setSourceTopic(record.topic());
                metadata.setEventType(eventType);
                metadata.setUserId(userId);
                metadata.setLatencyMs(latencyMs);
                metadata.setStatus(status);
                metadata.setPayload(record.value());
                metadata.setProcessedAt(Instant.now());
                metadata.setRetryCount(metadata.getRetryCount() + 1);
                metadataToSave.add(metadata);
            } catch (Exception e) {
                log.error("Failed to process event {} from topic {}: {}", record.key(), record.topic(), e.getMessage(), e);
            }
        }

        if (!metadataToSave.isEmpty()) {
            metadataRepository.saveAll(metadataToSave);
        }

        ack.acknowledge();
    }
}
