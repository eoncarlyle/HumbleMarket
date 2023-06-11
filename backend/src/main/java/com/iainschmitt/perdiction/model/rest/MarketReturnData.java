package com.iainschmitt.perdiction.model.rest;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;

import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.Outcome;

public class MarketReturnData extends Market {
    List<List<List<BigDecimal>>> salesPriceList;

    public MarketReturnData(int seqId, String question, String creatorId, int marketMakerK, long closeDate,
            List<Outcome> outcomes, boolean isPublic, boolean isClosed, boolean isResolved, List<List<List<BigDecimal>>> salesPriceList) {
        super(seqId, question, creatorId, marketMakerK, closeDate, outcomes, isPublic, isClosed, isResolved);
        this.salesPriceList = salesPriceList;
    }

    //TODO: Fix builder issue on this class and the parent class
    public static MarketReturnData of(Market market, List<List<List<BigDecimal>>> salesPriceList) {
        return new MarketReturnData(
            market.getSeqId(),
            market.getQuestion(),
            market.getCreatorId(),
            market.getMarketMakerK(),
            market.getCloseDate(),
            market.getOutcomes(),
            market.isPublic(),
            market.isClosed(),
            market.isResolved(),
            salesPriceList
        );
    }
}
