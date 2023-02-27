package com.iainschmitt.perdiction.model.rest;

import java.util.List;
import java.util.stream.Stream;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import com.iainschmitt.perdiction.model.Market;

@Data
@Builder
public class MarketData {
    // @Email
    // private String email;
    // @Pattern(regexp = "[a-zA-Z0-9!@#_\\-]{8,64}")
    // private String password;

    private String question;
    private String creatorId;
    private int marketMakerK;
    private long closeDate;
    private List<String> outcomeClaims;
    private boolean isPublic;

    public void validate() {
        // TODO: Implement this
    }

}
