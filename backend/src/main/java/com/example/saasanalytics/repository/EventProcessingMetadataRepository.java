package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.EventProcessingMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventProcessingMetadataRepository extends JpaRepository<EventProcessingMetadata, Long> {
    Optional<EventProcessingMetadata> findByTenantIdAndEventId(Long tenantId, String eventId);
}
