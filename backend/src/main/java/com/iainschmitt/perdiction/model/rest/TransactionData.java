package com.iainschmitt.perdiction.model.rest;

import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.TransactionType;

public class TransactionData {
    public TransactionType transactionType;
    public int outcomeIndex;
    public PositionDirection positionDirection;
    public int shares;
}
