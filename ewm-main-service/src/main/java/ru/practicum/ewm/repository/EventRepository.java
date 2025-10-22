package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findAllByIdIn(Collection<Long> ids);

    long countByCategoryId(long categoryId);

    long countByInitiatorId(long userId);

    long countByCategoryIdAndState(long categoryId, EventState state);

    Page<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    List<Event> findAllByInitiatorIdAndEventDateAfter(long initiatorId, LocalDateTime dateTime);
}
