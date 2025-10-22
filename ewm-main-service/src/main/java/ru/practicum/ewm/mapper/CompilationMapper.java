package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.model.Compilation;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class CompilationMapper {

    public Compilation toEntity(NewCompilationDto dto) {
        if (dto == null) {
            return null;
        }
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(Boolean.TRUE.equals(dto.getPinned()))
                .build();
    }

    public void updateEntity(Compilation compilation, UpdateCompilationRequest dto) {
        if (dto == null) {
            return;
        }
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
    }

    public CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {
        if (compilation == null) {
            return null;
        }
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events == null ? Collections.emptyList() : events)
                .build();
    }
}
