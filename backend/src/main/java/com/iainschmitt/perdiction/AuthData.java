package com.iainschmitt.perdiction;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthData {
    @Email
    private String email;
    @Pattern(regexp = "[a-zA-Z0-9!@#$%^&*()_+|;,./<>?]{8,64}")
    private String password;

    public void validate() {
        var validationRules = Validation
            .buildDefaultValidatorFactory()
            .getValidator()
            .validate(this);

        if (!validationRules.isEmpty()) {
            throw new ValidationException(
                String.format("AuthData object '%s' did not meet AuthData validation requirements", this)
            );
        }
    }
}
