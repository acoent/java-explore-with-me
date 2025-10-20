package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            SELECT h.app AS app, h.uri AS uri, COUNT(h) AS hits
            FROM EndpointHit h
            WHERE h.timestamp BETWEEN :start AND :end
            GROUP BY h.app, h.uri
            ORDER BY COUNT(h) DESC
            """)
    List<ViewStatsProjection> findAllStats(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
            SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits
            FROM EndpointHit h
            WHERE h.timestamp BETWEEN :start AND :end
            GROUP BY h.app, h.uri
            ORDER BY COUNT(DISTINCT h.ip) DESC
            """)
    List<ViewStatsProjection> findAllStatsUnique(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}
