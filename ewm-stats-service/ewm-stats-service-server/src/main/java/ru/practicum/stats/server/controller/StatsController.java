package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsConstants;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.MissingRequiredParameterException;
import ru.practicum.stats.server.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_FORMAT);

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.debug("Saving endpoint hit: {}", endpointHitDto);
        statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam(required = false) String start,
                                       @RequestParam(required = false) String end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") boolean unique) {
        if (start == null || start.isBlank()) {
            throw new MissingRequiredParameterException("start");
        }
        if (end == null || end.isBlank()) {
            throw new MissingRequiredParameterException("end");
        }

        LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

        log.debug("Requesting stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statsService.getStats(startDate, endDate, uris, unique);
    }
}
