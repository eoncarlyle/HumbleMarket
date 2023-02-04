package com.iainschmitt.perdiction;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class SignUpData {
    @Email
    private String email;
    @Pattern(regexp = "[a-zA-Z0-9!@#$%^&*()_+|;,./<>?]{8,32}")
    private String password;
}
