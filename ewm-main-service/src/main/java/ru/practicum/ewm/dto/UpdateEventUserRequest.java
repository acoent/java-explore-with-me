package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.model.UserStateAction;
import ru.practicum.stats.dto.StatsConstants;

import java.time.LocalDateTime;

@Value
@Builder
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000)
    String annotation;

    Long category;

    @Size(min = 20, max = 7000)
    String description;

    @Future
    @JsonFormat(pattern = StatsConstants.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @Valid
    LocationDto location;

    Boolean paid;

    Integer participantLimit;

    Boolean requestModeration;

    UserStateAction stateAction;

    @Size(min = 3, max = 120)
    String title;
}

