package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.client.StatsClientException;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private static final String APP_NAME = "ewm-main-service";

    private final StatsClient statsClient;
    private final Clock clock;

    public void hit(String uri, String ip) {
        if (uri == null || ip == null) {
            return;
        }
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now(clock))
                .build();
        try {
            statsClient.hit(hit);
        } catch (StatsClientException ex) {
            log.warn("Failed to register endpoint hit for uri={} ip={}: {}", uri, ip, ex.getMessage());
        }
    }

    public Map<Long, Long> getEventViews(Collection<Long> eventIds,
                                         LocalDateTime start,
                                         LocalDateTime end) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LocalDateTime actualStart = start == null ? LocalDateTime.now(clock).minusYears(10) : start;
        LocalDateTime actualEnd = end == null ? LocalDateTime.now(clock) : end;
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();
        try {
            List<ViewStatsDto> stats = statsClient.getStats(actualStart, actualEnd, uris, false);
            Map<Long, Long> result = new HashMap<>();
            for (ViewStatsDto stat : stats) {
                Long eventId = extractEventId(stat.getUri());
                if (eventId != null) {
                    result.merge(eventId, stat.getHits(), Long::sum);
                }
            }
            // ensure every requested event id is present
            eventIds.forEach(id -> result.putIfAbsent(id, 0L));
            return result;
        } catch (StatsClientException | IllegalArgumentException ex) {
            log.warn("Failed to retrieve stats: {}", ex.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

    private Long extractEventId(String uri) {
        if (uri == null) {
            return null;
        }
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == uri.length() - 1) {
            return null;
        }
        try {
            return Long.parseLong(uri.substring(lastSlash + 1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

