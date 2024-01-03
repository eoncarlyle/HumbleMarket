package com.iainschmitt.prediction.model.rest;

import lombok.Builder;

@Builder
public class LogInReturnData {
    public String message;
    public String token;
    public long expire;
}
