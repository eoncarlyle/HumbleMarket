package com.iainschmitt.prediction.model.rest;

import java.util.List;

import org.yaml.snakeyaml.error.Mark;

import lombok.Builder;

import com.iainschmitt.prediction.model.MarketProposalBasis;

@Builder
public class MarketProposalData extends MarketProposalBasis {
    public MarketProposalData(String question, String creatorId, int marketMakerK, long closeDate,
            List<String> outcomeClaims, boolean isPublic) {
        super(question, creatorId, marketMakerK, closeDate, outcomeClaims, isPublic);
    }

    public MarketProposalData() {
        super();
    }

    public static MarketProposalData of(String question, String creatorId, int marketMakerK, long closeDate,
            List<String> outcomeClaims, boolean isPublic) {
        return new MarketProposalData(question, creatorId, marketMakerK, closeDate, outcomeClaims, isPublic);
    }
}
