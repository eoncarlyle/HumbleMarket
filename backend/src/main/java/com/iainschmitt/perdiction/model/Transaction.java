package com.iainschmitt.perdiction.model;

import org.springframework.data.annotation.Id;
import lombok.Getter;

@Getter
public class Transaction {
    @Id
    private String id;
    private final String srcUserId;
    private final String dstUserId;
    private final String marketId;
    private final int outcomeIndex;
    private final PositionDirection direction;
    private final TransactionType transactionType;
    private final double credits;

    public Transaction(String srcUserId, String dstUserId, String marketId, int outcomeIndex,
            PositionDirection direction, TransactionType transactionType, double credits) {
        this.srcUserId = srcUserId;
        this.dstUserId = dstUserId;
        this.marketId = marketId;
        this.outcomeIndex = outcomeIndex;
        this.direction = direction;
        this.transactionType = transactionType;
        this.credits = credits;
    }
}
