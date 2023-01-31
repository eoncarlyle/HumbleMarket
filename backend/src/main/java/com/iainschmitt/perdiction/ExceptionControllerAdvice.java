package com.iainschmitt.perdiction;

import java.util.HashMap;

import com.google.gson.Gson;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> notFoundExceptionHandler(Exception e) {

        String body = "{\"status\": 404, \"body\": \"" + e.toString() + "\"}";
        return new
            ResponseEntity<>(
            body,
            HttpStatusCode.valueOf(404)
        );
    }
}

