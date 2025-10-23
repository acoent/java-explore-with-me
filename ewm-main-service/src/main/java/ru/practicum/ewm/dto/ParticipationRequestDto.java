package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.stats.dto.StatsConstants;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class ParticipationRequestDto {
    Long id;
    Long event;
    Long requester;
    @JsonFormat(pattern = StatsConstants.DATE_TIME_FORMAT)
    LocalDateTime created;
    RequestStatus status;
}

