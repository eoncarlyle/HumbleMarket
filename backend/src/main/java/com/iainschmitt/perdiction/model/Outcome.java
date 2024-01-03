package com.iainschmitt.prediction.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Outcome {
    private final String claim;
    private BigDecimal price;
    private int sharesY;
    private int sharesN;
}
