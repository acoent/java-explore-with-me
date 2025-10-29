package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.repository.projection.EventRequestCount;
import ru.practicum.ewm.repository.specification.EventSpecifications;
import ru.practicum.ewm.util.DateTimeUtil;
import ru.practicum.ewm.util.PaginationUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private static final int USER_EVENT_HOURS_BEFORE = 2;
    private static final int ADMIN_EVENT_HOURS_BEFORE = 1;

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsService statsService;
    private final Clock clock;

    @Transactional
    public EventFullDto createEvent(long userId, NewEventDto dto) {
        User initiator = getUser(userId);
        Category category = getCategory(dto.getCategory());
        validateEventDate(dto.getEventDate(), USER_EVENT_HOURS_BEFORE,
                "Event date must be at least %d hours in the future".formatted(USER_EVENT_HOURS_BEFORE));
        Event event = EventMapper.toEntity(dto, category, initiator, LocalDateTime.now(clock));
        Event saved = eventRepository.save(event);
        return toFullDto(saved);
    }

    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        ensureUserExists(userId);
        Pageable pageable = PaginationUtil.offsetPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        return toShortDtos(events);
    }

    public EventFullDto getUserEvent(long userId, long eventId) {
        Event event = getEventForInitiator(eventId, userId);
        return toFullDto(event);
    }

    @Transactional
    public EventFullDto updateEventByUser(long userId, long eventId, UpdateEventUserRequest request) {
        Event event = getEventForInitiator(eventId, userId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        if (request.getEventDate() != null) {
            validateEventDate(request.getEventDate(), USER_EVENT_HOURS_BEFORE,
                    "Event date must be at least %d hours in the future".formatted(USER_EVENT_HOURS_BEFORE));
        }
        if (request.getCategory() != null) {
            Category category = getCategory(request.getCategory());
            event.setCategory(category);
        }
        EventMapper.updateEventFromUser(event, request);
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }
        Event saved = eventRepository.save(event);
        return toFullDto(saved);
    }

    public List<EventFullDto> findEventsForAdmin(List<Long> userIds,
                                                 List<EventState> states,
                                                 List<Long> categories,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 int from,
                                                 int size) {
        LocalDateTime start = DateTimeUtil.parseOrNull(rangeStart);
        LocalDateTime end = DateTimeUtil.parseOrNull(rangeEnd);
        validateDateRange(start, end);

        Specification<Event> spec = Specification.where(EventSpecifications.hasInitiatorIds(userIds))
                .and(EventSpecifications.hasStates(states))
                .and(EventSpecifications.hasCategories(categories))
                .and(EventSpecifications.eventDateBetween(start, end));

        Pageable pageable = PaginationUtil.offsetPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        return toFullDtos(events);
    }

    @Transactional
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=%d was not found".formatted(eventId)));
        if (request.getEventDate() != null) {
            validateEventDate(request.getEventDate(), ADMIN_EVENT_HOURS_BEFORE,
                    "Event date must be at least %d hours in the future".formatted(ADMIN_EVENT_HOURS_BEFORE));
        }
        if (request.getCategory() != null) {
            Category category = getCategory(request.getCategory());
            event.setCategory(category);
        }
        EventMapper.updateEventFromAdmin(event, request);
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> publishEvent(event);
                case REJECT_EVENT -> rejectEvent(event);
            }
        }
        Event saved = eventRepository.save(event);
        return toFullDto(saved);
    }

    public List<EventShortDto> findPublicEvents(String text,
                                                List<Long> categories,
                                                Boolean paid,
                                                String rangeStart,
                                                String rangeEnd,
                                                Boolean onlyAvailable,
                                                String sort,
                                                int from,
                                                int size) {
        LocalDateTime start = DateTimeUtil.parseOrNull(rangeStart);
        LocalDateTime end = DateTimeUtil.parseOrNull(rangeEnd);
        if (start == null) {
            start = LocalDateTime.now(clock);
        }
        validateDateRange(start, end);

        Specification<Event> spec = Specification.where(EventSpecifications.isPublished())
                .and(EventSpecifications.hasText(text))
                .and(EventSpecifications.hasCategories(categories))
                .and(EventSpecifications.isPaid(paid))
                .and(EventSpecifications.eventDateBetween(start, end));

        EventSort sortType = parseSort(sort);
        List<Event> events;
        if (sortType == EventSort.EVENT_DATE) {
            Pageable pageable = PaginationUtil.offsetPageable(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));
            events = eventRepository.findAll(spec, pageable).getContent();
        } else {
            events = eventRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"));
        }

        Map<Long, Long> confirmed = getConfirmedRequests(events);
        if (Boolean.TRUE.equals(onlyAvailable)) {
            final Map<Long, Long> confirmedSnapshot = confirmed;
            events = events.stream()
                    .filter(event -> {
                        int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
                        if (limit == 0) {
                            return true;
                        }
                        long confirmedCount = confirmedSnapshot.getOrDefault(event.getId(), 0L);
                        return confirmedCount < limit;
                    })
                    .toList();
            confirmed = getConfirmedRequests(events);
        }

        Map<Long, Long> views = getViews(events);

        List<EventShortDto> dtos = toShortDtos(events, confirmed, views);

        if (sortType == EventSort.VIEWS) {
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed()
                    .thenComparing(EventShortDto::getEventDate));
            int startIndex = Math.min(from, dtos.size());
            int endIndex = Math.min(startIndex + size, dtos.size());
            return dtos.subList(startIndex, endIndex);
        }

        return dtos;
    }

    public EventFullDto getPublishedEvent(long eventId) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=%d was not found".formatted(eventId)));
        return toFullDto(event);
    }

    private Event getEventForInitiator(long eventId, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=%d was not found".formatted(eventId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Event with id=%d was not found".formatted(eventId));
        }
        return event;
    }

    private void publishEvent(Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Only pending events can be published");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now(clock).plusHours(ADMIN_EVENT_HOURS_BEFORE))) {
            throw new ConflictException("Event date must be at least %d hours in the future".formatted(ADMIN_EVENT_HOURS_BEFORE));
        }
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now(clock));
    }

    private void rejectEvent(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Published events cannot be rejected");
        }
        event.setState(EventState.CANCELED);
    }

    private void validateEventDate(LocalDateTime eventDate, int hoursAhead, String message) {
        if (eventDate == null) {
            return;
        }
        if (eventDate.isBefore(LocalDateTime.now(clock).plusHours(hoursAhead))) {
            throw new ValidationException(message);
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("Start must be before end");
        }
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=%d was not found".formatted(userId)));
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=%d was not found".formatted(userId));
        }
    }

    private Category getCategory(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=%d was not found".formatted(categoryId)));
    }

    private EventSort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return EventSort.EVENT_DATE;
        }
        try {
            return EventSort.valueOf(sort);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown sort: " + sort);
        }
    }

    private List<EventShortDto> toShortDtos(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> confirmed = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);
        return toShortDtos(events, confirmed, views);
    }

    private List<EventShortDto> toShortDtos(List<Event> events,
                                            Map<Long, Long> confirmed,
                                            Map<Long, Long> views) {
        if (events.isEmpty()) {
            return List.of();
        }
        List<EventShortDto> dtos = new ArrayList<>(events.size());
        for (Event event : events) {
            dtos.add(EventMapper.toShortDto(event,
                    confirmed.getOrDefault(event.getId(), 0L),
                    views.getOrDefault(event.getId(), 0L)));
        }
        return dtos;
    }

    private EventFullDto toFullDto(Event event) {
        return toFullDtos(List.of(event)).get(0);
    }

    private List<EventFullDto> toFullDtos(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> confirmed = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);
        return events.stream()
                .map(event -> EventMapper.toFullDto(event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        Map<Long, Long> result = new HashMap<>();
        if (events.isEmpty()) {
            return result;
        }
        List<Long> ids = events.stream().map(Event::getId).toList();
        List<EventRequestCount> counts = requestRepository.countConfirmedByEventIds(ids);
        for (EventRequestCount count : counts) {
            result.put(count.getEventId(), count.getRequestCount());
        }
        ids.forEach(id -> result.putIfAbsent(id, 0L));
        return result;
    }

    private Map<Long, Long> getViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = events.stream().map(Event::getId).toList();
        LocalDateTime earliest = events.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("Event must have creation timestamp"));
        LocalDateTime end = LocalDateTime.now(clock);
        return statsService.getEventViews(ids, earliest, end);
    }

    public List<EventShortDto> buildShortDtos(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        return toShortDtos(new ArrayList<>(events));
    }
}
