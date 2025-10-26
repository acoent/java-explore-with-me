package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.repository.projection.EventRequestCount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEventIdAndStatus(long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByEventId(long eventId);

    List<ParticipationRequest> findAllByRequesterId(long requesterId);

    Optional<ParticipationRequest> findByIdAndRequesterId(long requestId, long requesterId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(long eventId, long requesterId);

    List<ParticipationRequest> findAllByEventIdAndStatus(long eventId, RequestStatus status);

    @Query(value = "select * from requests where id in :ids for update", nativeQuery = true)
    List<ParticipationRequest> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);

    @Query("select pr.event.id as eventId, count(pr) as requestCount "
            + "from ParticipationRequest pr "
            + "where pr.event.id in :eventIds and pr.status = ru.practicum.ewm.model.RequestStatus.CONFIRMED "
            + "group by pr.event.id")
    List<EventRequestCount> countConfirmedByEventIds(Collection<Long> eventIds);
}
