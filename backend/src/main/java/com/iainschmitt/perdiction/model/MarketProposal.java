package com.iainschmitt.prediction.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.iainschmitt.prediction.model.rest.MarketProposalData;

public class MarketProposal extends MarketProposalBasis {
    @Getter
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
