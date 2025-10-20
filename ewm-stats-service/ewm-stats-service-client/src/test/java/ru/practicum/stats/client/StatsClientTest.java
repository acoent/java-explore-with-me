package ru.practicum.stats.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StatsClientTest {

    private RestTemplate restTemplate;
    private StatsClient statsClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        statsClient = new StatsClient(restTemplate, "http://localhost:9090");
    }

    @Test
    void hit_delegatesToRestTemplate() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("app")
                .uri("/uri")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.hit(dto);

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), eq(dto), eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
        assertThat(uriCaptor.getValue()).isEqualTo("http://localhost:9090/hit");
    }

    @Test
    void getStats_buildsRequestAndReturnsBody() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = start.plusHours(1);
        ViewStatsDto[] response = {ViewStatsDto.builder().app("app").uri("/uri").hits(5).build()};
        when(restTemplate.getForEntity(anyString(), eq(ViewStatsDto[].class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        List<ViewStatsDto> result = statsClient.getStats(start, end, List.of("/uri"), true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getHits()).isEqualTo(5);

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(uriCaptor.capture(), eq(ViewStatsDto[].class));
        String uri = uriCaptor.getValue();
        assertThat(uri)
                .startsWith("http://localhost:9090/stats?start=2024-01-01%2000:00:00&end=2024-01-01%2001:00:00");
        assertThat(uri).contains("unique=true");
        assertThat(uri).contains("uris=/uri");
    }

    @Test
    void getStats_throwsException_whenEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusMinutes(1);

        Throwable thrown = catchThrowable(() ->
                statsClient.getStats(start, end, null, false)
        );

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End must not be before start");
    }
}
