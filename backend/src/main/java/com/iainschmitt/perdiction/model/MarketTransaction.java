package com.iainschmitt.prediction.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import lombok.Getter;

@Getter
public class MarketTransaction {
    @Id
    private String id;
    private final String srcUserId;
    private final String dstUserId;
    private final String marketId;
    private final int outcomeIndex;
    private final PositionDirection direction;
    private final MarketTransactionType transactionType;
    private final BigDecimal credits;

    public MarketTransaction(String srcUserId, String dstUserId, String marketId, int outcomeIndex,
            PositionDirection direction, MarketTransactionType transactionType, BigDecimal credits) {
        this.srcUserId = srcUserId;
        this.dstUserId = dstUserId;
        this.marketId = marketId;
        this.outcomeIndex = outcomeIndex;
        this.direction = direction;
        this.transactionType = transactionType;
        this.credits = credits;
    }
}
