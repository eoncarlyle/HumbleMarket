package com.iainschmitt.prediction.model.rest;

import com.iainschmitt.prediction.model.PositionDirection;
import com.iainschmitt.prediction.model.MarketTransactionType;

public class MarketTransactionData {
    public MarketTransactionType transactionType;
    public int outcomeIndex;
    public PositionDirection positionDirection;
    public int shares;
}
