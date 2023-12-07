package com.iainschmitt.perdiction.model.rest;

import java.util.List;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;

import com.iainschmitt.perdiction.model.Market;

@AllArgsConstructor
public class MarketReturnData {
    public Market market;
    public List<List<List<BigDecimal>>> purchasePriceList;
    public List<List<List<BigDecimal>>> salePriceList;
    public BigDecimal userCredits;

    public static MarketReturnData of(Market market, List<List<List<BigDecimal>>> purchasePriceList,
            List<List<List<BigDecimal>>> salePriceList,
            BigDecimal userCredits) {
        return new MarketReturnData(market, purchasePriceList, salePriceList, userCredits);
    }
}
