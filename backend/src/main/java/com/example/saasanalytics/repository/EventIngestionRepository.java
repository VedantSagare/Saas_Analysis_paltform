package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.EventIngestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventIngestionRepository extends JpaRepository<EventIngestion, Long> {
    Optional<EventIngestion> findByTenantIdAndEventId(Long tenantId, String eventId);
}
