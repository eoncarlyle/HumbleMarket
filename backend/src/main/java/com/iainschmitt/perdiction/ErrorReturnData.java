package com.iainschmitt.perdiction;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public class ErrorReturnData {
    public int status;
    public String message;

    public static ErrorReturnData of(int status, String message) {
        return new ErrorReturnData(status, message);
    }
}
