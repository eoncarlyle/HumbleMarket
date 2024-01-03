package com.iainschmitt.prediction.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public abstract class MarketProposalBasis {
    private String question;
    private String creatorId;
    private int marketMakerK;
    private long closeDate;
    private List<String> outcomeClaims;
    private boolean isPublic;

    public MarketProposalBasis(String question, String creatorId, int marketMakerK, long closeDate,
            List<String> outcomeClaims, boolean isPublic) {
        this.question = question;
        this.creatorId = creatorId;
        this.marketMakerK = marketMakerK;
        this.closeDate = closeDate;
        this.outcomeClaims = outcomeClaims;
        this.isPublic = isPublic;
    }

    public MarketProposalBasis() {

    }
}
