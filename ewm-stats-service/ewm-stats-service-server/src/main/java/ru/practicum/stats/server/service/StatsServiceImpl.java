package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.InvalidDateRangeException;
import ru.practicum.stats.server.mapper.EndpointHitMapper;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.EndpointHitRepository;
import ru.practicum.stats.server.repository.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository endpointHitRepository;

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEntity(endpointHitDto);
        endpointHitRepository.save(endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        if (start == null || end == null) {
            throw new InvalidDateRangeException("Start and end dates must be specified");
        }
        if (end.isBefore(start)) {
            throw new InvalidDateRangeException("End date must not be before start date");
        }

        List<ViewStatsProjection> projections = unique
                ? endpointHitRepository.findAllStatsUnique(start, end)
                : endpointHitRepository.findAllStats(start, end);

        Stream<ViewStatsProjection> stream = projections.stream();
        if (uris != null && !uris.isEmpty()) {
            Set<String> uriFilter = new HashSet<>(uris);
            stream = stream.filter(projection -> uriFilter.contains(projection.getUri()));
        }

        return stream
                .map(EndpointHitMapper::toDto)
                .toList();
    }
}
