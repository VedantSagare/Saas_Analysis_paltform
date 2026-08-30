package com.example.saasanalytics.service;

import com.example.saasanalytics.domain.ApiKey;
import com.example.saasanalytics.domain.EventIngestion;
import com.example.saasanalytics.domain.Tenant;
import com.example.saasanalytics.repository.ApiKeyRepository;
import com.example.saasanalytics.repository.EventDeadLetterRepository;
import com.example.saasanalytics.repository.EventIngestionRepository;
import com.example.saasanalytics.web.dto.EventIngestionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private EventIngestionRepository eventIngestionRepository;

    @Mock
    private EventDeadLetterRepository eventDeadLetterRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ApiKeyService apiKeyService;

    @InjectMocks
    private EventIngestionService eventIngestionService;

    @Test
    void shouldRejectInvalidApiKey() {
        EventIngestionRequest request = new EventIngestionRequest();
        request.setEventId("evt-1");
        request.setEventType("page_view");
        request.setPayload(Map.of("url", "/home"));

        when(apiKeyService.validateApiKey("bad-key")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> eventIngestionService.ingest("bad-key", request));

        assertTrue(ex.getMessage().contains("API key"));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldRejectDuplicateEventIdForSameTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(10L);

        ApiKey apiKey = new ApiKey();
        apiKey.setTenant(tenant);

        EventIngestionRequest request = new EventIngestionRequest();
        request.setEventId("evt-duplicate");
        request.setEventType("page_view");
        request.setPayload(Map.of("url", "/home"));

        when(apiKeyService.validateApiKey("valid-key")).thenReturn(Optional.of(apiKey));
        when(eventIngestionRepository.findByTenantIdAndEventId(10L, "evt-duplicate"))
                .thenReturn(Optional.of(new EventIngestion()));

        EventIngestion result = eventIngestionService.ingest("valid-key", request);

        assertNotNull(result);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
}
