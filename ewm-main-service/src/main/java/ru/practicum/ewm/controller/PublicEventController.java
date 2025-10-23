package ru.practicum.ewm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.StatsService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final StatsService statsService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "10") @Positive int size,
                                         HttpServletRequest request) {
        statsService.hit(buildUri(request), resolveClientIp(request));
        return eventService.findPublicEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable long id, HttpServletRequest request) {
        statsService.hit(buildUri(request), resolveClientIp(request));
        return eventService.getPublishedEvent(id);
    }

    private String buildUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            uri += "?" + query;
        }
        return uri;
    }

    private String resolveClientIp(HttpServletRequest request) {
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
                // remove optional quotes
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
        // strip IPv6 brackets if present
        if (candidate.startsWith("[") && candidate.contains("]")) {
            int closing = candidate.indexOf(']');
            String inside = candidate.substring(1, closing);
            String remainder = candidate.substring(closing + 1);
            if (remainder.startsWith(":")) {
                return inside;
            }
            return inside;
        }
        // strip IPv4 port if present (e.g. 192.168.0.1:12345)
        int colonIndex = candidate.indexOf(':');
        if (colonIndex > 0 && candidate.indexOf('.') >= 0) {
            return candidate.substring(0, colonIndex);
        }
        return candidate;
    }
}
