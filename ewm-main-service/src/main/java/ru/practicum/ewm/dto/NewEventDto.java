package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import ru.practicum.stats.dto.StatsConstants;

import java.time.LocalDateTime;

@Value
@Builder
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotNull
    Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    @Future
    @JsonFormat(pattern = StatsConstants.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @NotNull
    @Valid
    LocationDto location;

    @Builder.Default
    Boolean paid = Boolean.FALSE;

    @PositiveOrZero
    @Builder.Default
    Integer participantLimit = 0;

    @Builder.Default
    Boolean requestModeration = Boolean.TRUE;

    @NotBlank
    @Size(min = 3, max = 120)
    String title;
}

