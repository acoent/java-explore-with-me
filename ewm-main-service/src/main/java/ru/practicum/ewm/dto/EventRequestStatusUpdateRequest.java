package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.ewm.model.RequestUpdateStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {

    @NotEmpty
    private List<Long> requestIds;

    @NotNull
    private RequestUpdateStatus status;
}
