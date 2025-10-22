package ru.practicum.stats.server.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.ViewStatsProjection;

public final class EndpointHitMapper {

    private EndpointHitMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }

        return EndpointHit.builder()
                .id(dto.getId())
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static ViewStatsDto toDto(ViewStatsProjection projection) {
        if (projection == null) {
            return null;
        }

        return ViewStatsDto.builder()
                .app(projection.getApp())
                .uri(projection.getUri())
                .hits(projection.getHits() == null ? 0L : projection.getHits())
                .build();
    }
}
