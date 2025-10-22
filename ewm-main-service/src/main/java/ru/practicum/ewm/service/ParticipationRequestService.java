package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.RequestUpdateStatus;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public List<ParticipationRequestDto> getUserRequests(long userId) {
        ensureUserExists(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Transactional
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        ensureUserExists(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=%d was not found".formatted(eventId)));
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in an unpublished event");
        }
        requestRepository.findByEventIdAndRequesterId(eventId, userId).ifPresent(existing -> {
            throw new ConflictException("Request already exists");
        });
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }
        RequestStatus status = (event.getRequestModeration() == null || !event.getRequestModeration() || limit == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;
        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("User with id=%d was not found".formatted(userId))))
                .created(LocalDateTime.now(clock))
                .status(status)
                .build();
        ParticipationRequest saved = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(saved);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=%d was not found".formatted(requestId)));
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(saved);
    }

    public List<ParticipationRequestDto> getEventRequests(long userId, long eventId) {
        Event event = getEventForInitiator(userId, eventId);
        return requestRepository.findAllByEventId(event.getId()).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(long userId,
                                                              long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        Event event = getEventForInitiator(userId, eventId);
        if (request.getRequestIds() == null || request.getRequestIds().isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }
        List<ParticipationRequest> requests = requestRepository.findAllByIdInForUpdate(request.getRequestIds());
        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("One or more requests were not found");
        }
        for (ParticipationRequest participationRequest : requests) {
            if (!Objects.equals(participationRequest.getEvent().getId(), eventId)) {
                throw new ConflictException("Request does not belong to the event");
            }
            if (participationRequest.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only pending requests can be updated");
            }
        }

        RequestUpdateStatus status = request.getStatus();
        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();
        int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        if (status == RequestUpdateStatus.CONFIRMED) {
            long alreadyConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            for (ParticipationRequest participationRequest : requests) {
                if (limit > 0 && alreadyConfirmed >= limit) {
                    participationRequest.setStatus(RequestStatus.REJECTED);
                    rejected.add(participationRequest);
                    continue;
                }
                participationRequest.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(participationRequest);
                alreadyConfirmed++;
            }
        } else {
            for (ParticipationRequest participationRequest : requests) {
                participationRequest.setStatus(RequestStatus.REJECTED);
                rejected.add(participationRequest);
            }
        }
        requestRepository.saveAll(requests);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream()
                        .map(ParticipationRequestMapper::toDto)
                        .toList())
                .rejectedRequests(rejected.stream()
                        .map(ParticipationRequestMapper::toDto)
                        .toList())
                .build();
    }

    private Event getEventForInitiator(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=%d was not found".formatted(eventId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Event with id=%d was not found".formatted(eventId));
        }
        return event;
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=%d was not found".formatted(userId));
        }
    }
}

