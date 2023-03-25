package com.iainschmitt.perdiction.model.rest;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountReturnData {
    private String email;
    private double credits;
    private List<PositionReturnData> positionsReturnData;
}
