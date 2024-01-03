package com.iainschmitt.prediction.model.rest;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

import com.iainschmitt.prediction.model.PositionDirection;

@Getter
@Setter
public class MarketTransactionRequestData {
    public String id;
    public int outcomeIndex;
    public PositionDirection positionDirection;
    public int shares;
    public BigDecimal sharePrice;
}
