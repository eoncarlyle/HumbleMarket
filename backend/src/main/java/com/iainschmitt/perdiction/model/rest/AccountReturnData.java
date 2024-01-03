package com.iainschmitt.prediction.model.rest;

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
    private BigDecimal credits;
    private List<PositionReturnData> positionsReturnData;
}
