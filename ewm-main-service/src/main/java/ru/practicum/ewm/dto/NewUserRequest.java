package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {

    @Email
    @NotBlank
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

    @JsonIgnore
    @AssertTrue(message = "Email local part must be at most 64 characters and domain at most 63 characters")
    public boolean isEmailPartsLengthValid() {
        if (email == null) {
            return true;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return true;
        }
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);
        return localPart.length() <= 64 && domainPart.length() <= 63;
    }
}
