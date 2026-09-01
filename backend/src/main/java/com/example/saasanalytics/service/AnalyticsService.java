package com.example.saasanalytics.service;

import com.example.saasanalytics.domain.EventProcessingMetadata;
import com.example.saasanalytics.repository.EventProcessingMetadataRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final EventProcessingMetadataRepository metadataRepository;

    public AnalyticsService(EventProcessingMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public AnalyticsSummary getSummary(Long tenantId, Instant from, Instant to) {
        List<EventProcessingMetadata> events = metadataRepository.findByTenantIdAndCreatedAtBetween(tenantId, from, to);
        List<String> distinctUserIds = metadataRepository.findDistinctUserIdsByTenantIdAndCreatedAtBetween(tenantId, from, to);

        Map<String, Long> eventCounts = new LinkedHashMap<>();
        for (Object[] row : metadataRepository.countByEventTypeAndTenantIdAndCreatedAtBetween(tenantId, from, to)) {
            String eventType = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            eventCounts.put(eventType, count);
        }

        Map<String, Long> topEvents = eventCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        List<Long> latencyValues = metadataRepository.findLatencyMsByTenantIdAndCreatedAtBetween(tenantId, from, to);
        double p95 = latencyValues.isEmpty() ? 0 : percentile(latencyValues, 95.0);
        double p99 = latencyValues.isEmpty() ? 0 : percentile(latencyValues, 99.0);

        List<FunnelStage> funnels = buildFunnels(events);
        List<RetentionBucket> retention = buildRetention(events);

        return new AnalyticsSummary(distinctUserIds.size(), eventCounts, topEvents, funnels, retention,
                new LatencyMetrics(p95, p99));
    }

    private List<FunnelStage> buildFunnels(List<EventProcessingMetadata> events) {
        Map<String, Long> byEventType = events.stream()
                .filter(e -> e.getEventType() != null)
                .collect(Collectors.groupingBy(EventProcessingMetadata::getEventType, Collectors.counting()));

        List<FunnelStage> stages = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, Long> entry : byEventType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .toList()) {
            stages.add(new FunnelStage(index++, entry.getKey(), entry.getValue()));
        }
        return stages;
    }

    private List<RetentionBucket> buildRetention(List<EventProcessingMetadata> events) {
        Map<String, Long> byUser = events.stream()
                .filter(e -> e.getUserId() != null)
                .collect(Collectors.groupingBy(EventProcessingMetadata::getUserId, Collectors.counting()));

        List<RetentionBucket> buckets = new ArrayList<>();
        if (!byUser.isEmpty()) {
            buckets.add(new RetentionBucket(1, byUser.size()));
        }
        return buckets;
    }

    private double percentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    public record AnalyticsSummary(long dailyActiveUsers,
                                  Map<String, Long> eventCounts,
                                  Map<String, Long> topEvents,
                                  List<FunnelStage> funnels,
                                  List<RetentionBucket> retention,
                                  LatencyMetrics latency) {
    }

    public record FunnelStage(int stage, String eventType, long count) {
    }

    public record RetentionBucket(int day, long users) {
    }

    public record LatencyMetrics(double p95Ms, double p99Ms) {
    }
}
