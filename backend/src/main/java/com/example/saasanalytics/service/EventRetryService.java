package com.example.saasanalytics.service;

import com.example.saasanalytics.domain.EventProcessingMetadata;
import com.example.saasanalytics.repository.EventProcessingMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class EventRetryService {

    private final EventProcessingMetadataRepository metadataRepository;

    public EventRetryService(EventProcessingMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Transactional
    public void markRetry(String eventId, Long tenantId, String cause) {
        EventProcessingMetadata metadata = metadataRepository.findByTenantIdAndEventId(tenantId, eventId)
                .orElseGet(() -> {
                    EventProcessingMetadata newMeta = new EventProcessingMetadata();
                    newMeta.setEventId(eventId);
                    newMeta.setTenantId(tenantId);
                    newMeta.setSourceTopic("events");
                    newMeta.setStatus("RETRYING");
                    return newMeta;
                });

        metadata.setStatus("RETRYING");
        metadata.setRetryCount(metadata.getRetryCount() + 1);
        metadata.setProcessedAt(Instant.now());
        metadataRepository.save(metadata);
    }

    public List<EventProcessingMetadata> getRetryCandidates() {
        return metadataRepository.findAll().stream()
                .filter(m -> "RETRYING".equals(m.getStatus()))
                .toList();
    }
}
