package com.iainschmitt.perdiction.model.rest;

import lombok.Getter;
import lombok.Setter;

import com.iainschmitt.perdiction.model.PositionDirection;

@Setter
@Getter
public class PurchaseRequestData {
    public int seqId;
    public int outcomeIndex;
    public PositionDirection positionDirection;
    public int shares;
}
