package com.iainschmitt.perdiction.model.rest;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketCreationData {
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
