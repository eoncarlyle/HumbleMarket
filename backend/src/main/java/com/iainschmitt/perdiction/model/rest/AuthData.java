package com.iainschmitt.perdiction.model.rest;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthData {
    @Email
    private String email;
    @Pattern(regexp = "[a-zA-Z0-9]{64}")
    private String passwordHash;

    public void validate() {
        var validationRules = Validation.buildDefaultValidatorFactory().getValidator().validate(this);

        if (!validationRules.isEmpty()) {
            throw new ValidationException(
                    String.format("AuthData object '%s' did not meet AuthData validation requirements", this));
        }
    }

    public static AuthData of(String email, String passwordHash) {
        return new AuthData() {{
                setEmail(email);
                setPasswordHash(passwordHash);
            }};
    }
}
