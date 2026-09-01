package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.EventProcessingMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventProcessingMetadataRepository extends JpaRepository<EventProcessingMetadata, Long> {
    Optional<EventProcessingMetadata> findByTenantIdAndEventId(Long tenantId, String eventId);

    List<EventProcessingMetadata> findByTenantIdAndCreatedAtBetween(Long tenantId, Instant from, Instant to);

    @Query("SELECT DISTINCT e.userId FROM EventProcessingMetadata e WHERE e.tenantId = :tenantId AND e.createdAt BETWEEN :from AND :to AND e.userId IS NOT NULL")
    List<String> findDistinctUserIdsByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId,
                                                                @Param("from") Instant from,
                                                                @Param("to") Instant to);

    @Query("SELECT e.eventType, COUNT(e) FROM EventProcessingMetadata e WHERE e.tenantId = :tenantId AND e.createdAt BETWEEN :from AND :to GROUP BY e.eventType")
    List<Object[]> countByEventTypeAndTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId,
                                                                 @Param("from") Instant from,
                                                                 @Param("to") Instant to);

    @Query("SELECT e.latencyMs FROM EventProcessingMetadata e WHERE e.tenantId = :tenantId AND e.createdAt BETWEEN :from AND :to AND e.latencyMs IS NOT NULL")
    List<Long> findLatencyMsByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId,
                                                         @Param("from") Instant from,
                                                         @Param("to") Instant to);
}
