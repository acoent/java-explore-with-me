package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.PaginationUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toEntity(dto);
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            compilation.setEvents(new HashSet<>(resolveEvents(dto.getEvents())));
        }
        Compilation saved = compilationRepository.save(compilation);
        return toDto(saved);
    }

    @Transactional
    public void deleteCompilation(long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new NotFoundException("Compilation with id=%d was not found".formatted(compilationId));
        }
        compilationRepository.deleteById(compilationId);
    }

    @Transactional
    public CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=%d was not found".formatted(compilationId)));
        CompilationMapper.updateEntity(compilation, request);
        if (request.getEvents() != null) {
            compilation.setEvents(new HashSet<>(resolveEvents(request.getEvents())));
        }
        Compilation saved = compilationRepository.save(compilation);
        return toDto(saved);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PaginationUtil.offsetPageable(from, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }
        return compilations.stream()
                .map(this::toDto)
                .toList();
    }

    public CompilationDto getCompilation(long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=%d was not found".formatted(compilationId)));
        return toDto(compilation);
    }

    private CompilationDto toDto(Compilation compilation) {
        List<Event> events = compilation.getEvents().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
        List<EventShortDto> eventDtos = eventService.buildShortDtos(events);
        return CompilationMapper.toDto(compilation, eventDtos);
    }

    private List<Event> resolveEvents(List<Long> ids) {
        List<Event> events = eventRepository.findAllById(ids);
        if (events.size() != ids.size()) {
            throw new NotFoundException("One or more events were not found to build compilation");
        }
        return events;
    }
}
