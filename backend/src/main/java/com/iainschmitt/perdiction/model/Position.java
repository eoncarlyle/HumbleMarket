package com.iainschmitt.prediction.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Position {
    @Id
    private String id;
    private String userId;
    private final String marketId;
    private final int outcomeIndex;
    private final PositionDirection direction;
    private final int shares;
    @Setter
    private BigDecimal priceAtBuy;

    public Position(String userId, String marketId, int outcomeIndex, PositionDirection direction, int shares,
            BigDecimal priceAtBuy) {
        this.userId = userId;
        this.marketId = marketId;
        this.outcomeIndex = outcomeIndex;
        this.direction = direction;
        this.shares = shares;
        this.priceAtBuy = priceAtBuy;
    }
}
