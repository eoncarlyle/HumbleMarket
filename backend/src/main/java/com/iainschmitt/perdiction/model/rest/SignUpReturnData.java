package com.iainschmitt.prediction.model.rest;

import lombok.Builder;

@Builder
public class SignUpReturnData {
    public String message;
    public String email;
    public String token;
    public long expire;
}
