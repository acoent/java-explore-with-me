package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.model.UserStateAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest extends BaseUpdateEventRequest {

    private UserStateAction stateAction;
}
