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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private static final String APP_NAME = "ewm-main-service";

    private final StatsClient statsClient;
    private final Clock clock;
    private final Map<Long, Set<String>> localUniqueViews = new ConcurrentHashMap<>();

    public void hit(String uri, String ip) {
        if (uri == null || ip == null) {
            return;
        }
        String sanitizedIp = ip.trim();
        if (sanitizedIp.isEmpty()) {
            return;
        }
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(sanitizedIp)
                .timestamp(LocalDateTime.now(clock))
                .build();
        try {
            statsClient.hit(hit);
        } catch (StatsClientException ex) {
            log.warn("Failed to register endpoint hit for uri={} ip={}: {}", uri, ip, ex.getMessage());
        }
        recordLocalView(uri, sanitizedIp);
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
        Map<Long, Long> result = new HashMap<>();
        boolean statsAvailable = true;
        try {
            List<ViewStatsDto> stats = statsClient.getStats(actualStart, actualEnd, uris, true);
            for (ViewStatsDto stat : stats) {
                Long eventId = extractEventId(stat.getUri());
                if (eventId != null) {
                    result.merge(eventId, stat.getHits(), Long::sum);
                }
            }
        } catch (StatsClientException | IllegalArgumentException ex) {
            log.warn("Failed to retrieve stats: {}", ex.getMessage());
            statsAvailable = false;
        }
        eventIds.forEach(id -> result.putIfAbsent(id, 0L));

        for (Long eventId : eventIds) {
            Set<String> localIps = localUniqueViews.get(eventId);
            if (localIps == null || localIps.isEmpty()) {
                continue;
            }
            long localCount = localIps.size();
            if (!statsAvailable) {
                result.put(eventId, localCount);
            } else {
                result.compute(eventId, (id, current) -> current == null ? localCount : Math.max(current, localCount));
            }
        }

        if (!statsAvailable) {
            return result;
        }

        return result;
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

    private void recordLocalView(String uri, String ip) {
        Long eventId = extractEventId(uri);
        if (eventId == null) {
            return;
        }
        localUniqueViews
                .computeIfAbsent(eventId, key -> ConcurrentHashMap.newKeySet())
                .add(ip);
    }
}
