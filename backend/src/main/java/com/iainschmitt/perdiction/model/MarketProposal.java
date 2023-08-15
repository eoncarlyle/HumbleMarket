package com.iainschmitt.perdiction.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.iainschmitt.perdiction.model.rest.MarketCreationData;

public class MarketProposal extends MarketCreationData {
    @Id
    private String id;


    // The 500 code on market creation is some annoying problem related to typing the response body on 
    public MarketProposal(String question, String creatorId, int marketMakerK, long closeDate,
            List<String> outcomeClaims, boolean isPublic) {
        super.builder()
            .question(question)
            .creatorId(creatorId)
            .marketMakerK(marketMakerK)
            .closeDate(closeDate)
            .outcomeClaims(outcomeClaims)
            .isPublic(isPublic)
            .build();
    }

    public static MarketProposal of(MarketCreationData marketCreationData) {
        return new MarketProposal(marketCreationData.getQuestion(), marketCreationData.getCreatorId(),
                marketCreationData.getMarketMakerK(), marketCreationData.getCloseDate(),
                marketCreationData.getOutcomeClaims(), marketCreationData.isPublic());
    }
}
