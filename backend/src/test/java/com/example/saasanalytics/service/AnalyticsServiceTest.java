package com.example.saasanalytics.service;

import com.example.saasanalytics.domain.EventProcessingMetadata;
import com.example.saasanalytics.repository.EventProcessingMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EventProcessingMetadataRepository metadataRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void shouldReturnSummaryMetricsForTenant() {
        EventProcessingMetadata page = new EventProcessingMetadata();
        page.setUserId("u1");
        page.setEventType("page_view");
        page.setCreatedAt(Instant.now());
        page.setLatencyMs(150L);

        EventProcessingMetadata signup = new EventProcessingMetadata();
        signup.setUserId("u2");
        signup.setEventType("signup");
        signup.setCreatedAt(Instant.now());
        signup.setLatencyMs(250L);

        when(metadataRepository.findByTenantIdAndCreatedAtBetween(any(Long.class), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(page, signup));
        when(metadataRepository.findDistinctUserIdsByTenantIdAndCreatedAtBetween(any(Long.class), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of("u1", "u2"));
        when(metadataRepository.countByEventTypeAndTenantIdAndCreatedAtBetween(any(Long.class), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(new Object[]{"page_view", 1L}, new Object[]{"signup", 1L}));
        when(metadataRepository.findLatencyMsByTenantIdAndCreatedAtBetween(any(Long.class), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(150L, 250L));

        var summary = analyticsService.getSummary(7L, Instant.now().minusSeconds(3600), Instant.now());

        assertNotNull(summary);
        assertEquals(2, summary.dailyActiveUsers());
        assertFalse(summary.eventCounts().isEmpty());
        assertFalse(summary.latency().p95Ms() == 0);
    }
}
