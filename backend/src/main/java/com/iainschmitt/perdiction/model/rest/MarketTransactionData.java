package com.iainschmitt.perdiction.model.rest;

import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.MarketTransactionType;

public class MarketTransactionData {
    public MarketTransactionType transactionType;
    public int outcomeIndex;
    public PositionDirection positionDirection;
    public int shares;
}
