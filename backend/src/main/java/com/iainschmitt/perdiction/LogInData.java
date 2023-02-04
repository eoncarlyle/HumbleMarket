package com.iainschmitt.perdiction;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class LogInData {
    @Email
    private String email;
    @Pattern(regexp = "[a-zA-Z0-9!@#$%^&*()_+|;,./<>?]{8,32}")
    private String password;
}
