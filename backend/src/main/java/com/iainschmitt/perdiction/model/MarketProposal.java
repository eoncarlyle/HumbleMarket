package com.iainschmitt.perdiction.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.iainschmitt.perdiction.model.rest.MarketProposalData;

public class MarketProposal extends MarketProposalBasis {
    @Id
    private String id;

    public MarketProposal(String question, String creatorId, int marketMakerK, long closeDate,
    List<String> outcomeClaims, boolean isPublic) {
        super(question, creatorId, marketMakerK, closeDate, outcomeClaims, isPublic);
    }

    public static MarketProposal of(MarketProposalData marketCreationData) {
        return new MarketProposal(marketCreationData.getQuestion(), marketCreationData.getCreatorId(),
                marketCreationData.getMarketMakerK(), marketCreationData.getCloseDate(),
                marketCreationData.getOutcomeClaims(), marketCreationData.isPublic());
    }
}
