package ru.practicum.stats.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsConstants;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StatsClient {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_FORMAT);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StatsClient(String baseUrl) {
        this(new RestTemplate(), baseUrl);
    }

    public StatsClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate must not be null");
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public void hit(EndpointHitDto endpointHitDto) {
        Objects.requireNonNull(endpointHitDto, "endpointHitDto must not be null");

        try {
            restTemplate.postForEntity(baseUrl + "/hit", endpointHitDto, Void.class);
        } catch (RestClientException ex) {
            throw new StatsClientException("Failed to register endpoint hit", ex);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must not be before start");
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris.toArray());
        }

        String uri = uriBuilder.encode().toUriString();

        try {
            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(uri, ViewStatsDto[].class);
            ViewStatsDto[] body = response.getBody();
            if (body == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(body).toList();
        } catch (RestClientException ex) {
            throw new StatsClientException("Failed to retrieve statistics", ex);
        }
    }

    private String normalizeBaseUrl(String url) {
        Objects.requireNonNull(url, "baseUrl must not be null");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
