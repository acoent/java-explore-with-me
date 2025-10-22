package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.client.StatsClientException;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2024-07-20T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private StatsClient statsClient;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(statsClient, clock);
    }

    @Test
    @DisplayName("hit delegates call to stats client")
    void hitDelegatesToClient() {
        statsService.hit("/events", "127.0.0.1");

        ArgumentCaptor<EndpointHitDto> captor = ArgumentCaptor.forClass(EndpointHitDto.class);
        verify(statsClient).hit(captor.capture());
        EndpointHitDto hit = captor.getValue();
        assertThat(hit.getApp()).isEqualTo("ewm-main-service");
        assertThat(hit.getUri()).isEqualTo("/events");
        assertThat(hit.getIp()).isEqualTo("127.0.0.1");
        assertThat(hit.getTimestamp())
                .withFailMessage("Timestamp should be generated using the provided clock")
                .isEqualTo(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
    }

    @Test
    @DisplayName("getEventViews returns map of hits keyed by event id")
    void getEventViewsReturnsHits() {
        when(statsClient.getStats(any(), any(), anyList(), eq(false))).thenReturn(List.of(
                ViewStatsDto.builder().app("ewm-main-service").uri("/events/1").hits(5).build()
        ));

        Map<Long, Long> result = statsService.getEventViews(List.of(1L, 2L),
                LocalDateTime.now(clock).minusDays(1),
                LocalDateTime.now(clock));

        assertThat(result).containsEntry(1L, 5L);
        assertThat(result).containsEntry(2L, 0L);
    }

    @Test
    @DisplayName("getEventViews returns zeros when client fails")
    void getEventViewsHandlesException() {
        doThrow(new StatsClientException("fail", new RuntimeException()))
                .when(statsClient).getStats(any(), any(), anyList(), eq(false));

        Map<Long, Long> result = statsService.getEventViews(List.of(1L, 2L),
                LocalDateTime.now(clock).minusDays(1),
                LocalDateTime.now(clock));

        assertThat(result).containsEntry(1L, 0L);
        assertThat(result).containsEntry(2L, 0L);
    }
}
