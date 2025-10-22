package ru.practicum.ewm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NewUserRequest {

    @Email
    @NotBlank
    @Size(max = 254)
    String email;

    @NotBlank
    @Size(max = 128)
    String name;
}

