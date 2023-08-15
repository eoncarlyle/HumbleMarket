package com.iainschmitt.perdiction.model.rest;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MarketCreationData {
    private String question;
    private String creatorId;
    private int marketMakerK;
    private long closeDate;
    private List<String> outcomeClaims;
    private boolean isPublic;
}
