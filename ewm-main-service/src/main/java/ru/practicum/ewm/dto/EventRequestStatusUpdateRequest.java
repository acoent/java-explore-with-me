package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.model.RequestUpdateStatus;

import java.util.List;

@Value
@Builder
public class EventRequestStatusUpdateRequest {

    @NotEmpty
    List<Long> requestIds;

    @NotNull
    RequestUpdateStatus status;
}

