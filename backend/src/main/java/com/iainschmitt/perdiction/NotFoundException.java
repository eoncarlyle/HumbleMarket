package com.iainschmitt.perdiction;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message) {
        super(message);
    }
    public NotFoundException(String messageTemplate, String templatedValue) {
        super(String.format(messageTemplate, templatedValue));
    }
    public NotFoundException(){}

}