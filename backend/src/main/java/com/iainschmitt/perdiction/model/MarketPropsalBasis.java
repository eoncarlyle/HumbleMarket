package com.iainschmitt.perdiction.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class MarketPropsalBasis {
    private String question;
    private String creatorId;
    private int marketMakerK;
    private long closeDate;
    private List<String> outcomeClaims;
    private boolean isPublic;
}
