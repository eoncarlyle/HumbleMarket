package com.iainschmitt.perdiction.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Outcome {
    private final String claim;
    private float price;
    private int sharesY;
    private int sharesN;
}
