package com.iainschmitt.perdiction.model.rest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ErrorReturnData {
    public int status;
    public String message;

    public static ErrorReturnData of(int status, String message) {
        return new ErrorReturnData(status, message);
    }
}
