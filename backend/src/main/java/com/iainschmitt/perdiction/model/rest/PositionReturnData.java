package com.iainschmitt.perdiction.model.rest;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.iainschmitt.perdiction.model.PositionDirection;

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
