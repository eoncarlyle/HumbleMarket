package com.iainschmitt.perdiction;

import lombok.Builder;

@Builder
public class SignUpReturnData {
    public String message;
    public String email;
    public String token;
    public long expire;
}
