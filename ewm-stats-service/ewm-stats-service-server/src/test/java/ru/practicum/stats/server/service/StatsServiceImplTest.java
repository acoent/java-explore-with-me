package ru.practicum.stats.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.InvalidDateRangeException;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.EndpointHitRepository;
import ru.practicum.stats.server.repository.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsServiceImplTest {

    private StatsService statsService;

    @Mock
    private EndpointHitRepository endpointHitRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        statsService = new StatsServiceImpl(endpointHitRepository);
    }

    @Test
    void saveHit_persistsEntity() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        statsService.saveHit(dto);

        ArgumentCaptor<EndpointHit> captor = ArgumentCaptor.forClass(EndpointHit.class);
        verify(endpointHitRepository).save(captor.capture());
        EndpointHit saved = captor.getValue();
        assertThat(saved.getApp()).isEqualTo(dto.getApp());
        assertThat(saved.getUri()).isEqualTo(dto.getUri());
    }

    @Test
    void getStats_throwsException_whenEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusMinutes(1);

        Throwable thrown = catchThrowable(() ->
                statsService.getStats(start, end, null, false)
        );

        assertThat(thrown)
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessage("End date must not be before start date");
    }

    @Test
    void getStats_filtersByUris() {
        LocalDateTime now = LocalDateTime.now();
        when(endpointHitRepository.findAllStats(now.minusHours(1), now)).thenReturn(List.of(
                projection("app", "/a", 5),
                projection("app", "/b", 10)
        ));

        List<ViewStatsDto> result = statsService.getStats(now.minusHours(1), now, List.of("/b"), false);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUri()).isEqualTo("/b");
        assertThat(result.getFirst().getHits()).isEqualTo(10);
    }

    @Test
    void getStats_usesUniqueHits_whenRequested() {
        LocalDateTime now = LocalDateTime.now();
        when(endpointHitRepository.findAllStatsUnique(now.minusHours(1), now)).thenReturn(List.of(
                projection("app", "/a", 3)
        ));

        List<ViewStatsDto> result = statsService.getStats(now.minusHours(1), now, null, true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getHits()).isEqualTo(3);
    }

    private ViewStatsProjection projection(String app, String uri, long hits) {
        return new ViewStatsProjection() {
            @Override
            public String getApp() {
                return app;
            }

            @Override
            public String getUri() {
                return uri;
            }

            @Override
            public Long getHits() {
                return hits;
            }
        };
    }
}
