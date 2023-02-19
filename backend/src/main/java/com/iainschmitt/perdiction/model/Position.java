package com.iainschmitt.perdiction.model;

import org.springframework.data.annotation.Id;

import lombok.Getter;

@Getter
public class Position {
    @Id
    private String id;
    private String userId;
    private final String marketId;
    private final Outcome outcome;
    private final PositionDirection direction;
    private final int shares;
    private CreditValue priceAtBuy;

    public Position(String userId, String marketId, Outcome outcome, PositionDirection direction, int shares,
            CreditValue priceAtBuy) {
        this.userId = userId;
        this.marketId = marketId;
        this.outcome = outcome;
        this.direction = direction;
        this.shares = shares;
        this.priceAtBuy = priceAtBuy;
    }
}
