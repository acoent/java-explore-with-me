package ru.practicum.ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class StatsService {

    private static final String DEFAULT_APP_NAME = "ewm-main-service";

    private final StatsClient statsClient;
    private final Clock clock;
    private final String appName;
    private final Map<Long, Set<String>> localUniqueViews = new ConcurrentHashMap<>();

    public StatsService(StatsClient statsClient,
                        Clock clock,
                        @Value("${ewm.app-name:" + DEFAULT_APP_NAME + "}") String appName) {
        this.statsClient = statsClient;
        this.clock = clock;
        this.appName = appName;
    }

    public void hit(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        String uri = buildUri(request);
        String ip = resolveClientIp(request);
        if (uri == null || ip == null) {
            return;
        }
        String sanitizedIp = ip.trim();
        if (sanitizedIp.isEmpty()) {
            return;
        }
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(resolveAppName())
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

    private String resolveAppName() {
        return (appName == null || appName.isBlank()) ? DEFAULT_APP_NAME : appName;
    }

    private String buildUri(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            uri += "?" + query;
        }
        return uri;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = extractFromForwardedFor(request.getHeader("X-Forwarded-For"));
        if (ip == null) {
            ip = extractFromForwardedHeader(request.getHeader("Forwarded"));
        }
        if (ip == null) {
            ip = sanitizeIp(request.getHeader("X-Real-IP"));
        }
        if (ip == null) {
            ip = sanitizeIp(request.getRemoteAddr());
        }
        return ip;
    }

    private String extractFromForwardedFor(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String[] parts = header.split(",");
        for (String part : parts) {
            String candidate = sanitizeIp(part);
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }

    private String extractFromForwardedHeader(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String[] segments = header.split(";");
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (trimmed.toLowerCase().startsWith("for=")) {
                String value = trimmed.substring(4);
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return sanitizeIp(value);
            }
        }
        return null;
    }

    private String sanitizeIp(String value) {
        if (value == null) {
            return null;
        }
        String candidate = value.trim();
        if (candidate.isEmpty()) {
            return null;
        }
        if (candidate.startsWith("[") && candidate.contains("]")) {
            int closing = candidate.indexOf(']');
            String inside = candidate.substring(1, closing);
            String remainder = candidate.substring(closing + 1);
            if (remainder.startsWith(":")) {
                return inside;
            }
            return inside;
        }
        int colonIndex = candidate.indexOf(':');
        if (colonIndex > 0 && candidate.indexOf('.') >= 0) {
            return candidate.substring(0, colonIndex);
        }
        return candidate;
    }
}
