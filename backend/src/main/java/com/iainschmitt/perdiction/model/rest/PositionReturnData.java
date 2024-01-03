package com.iainschmitt.prediction.model.rest;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.iainschmitt.prediction.model.PositionDirection;

@Getter
@Setter
@Builder
public class PositionReturnData {
    private String marketQuestion;
    private int marketSeqId;
    private String outcomeClaim;
    private PositionDirection direction;
    private int shares;
    private BigDecimal priceAtBuy;
}
