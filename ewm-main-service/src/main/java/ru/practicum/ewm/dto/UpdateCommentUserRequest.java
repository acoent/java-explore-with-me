package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateCommentUserRequest {

    @NotBlank
    @Size(min = 1, max = 1000)
    String text;
}

