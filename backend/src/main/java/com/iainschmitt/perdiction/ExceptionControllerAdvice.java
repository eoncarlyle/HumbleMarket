package com.iainschmitt.perdiction;


import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

    public ResponseEntity<ErrorReturnData> createErrorReturnEntity(Exception e, int status) {
        return new
            ResponseEntity<>(
            ErrorReturnData.of(status, e.getMessage()),
            HttpStatusCode.valueOf(status)
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorReturnData> validationExceptionHandler(Exception e) {
        return createErrorReturnEntity(e, 422);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorReturnData> notFoundExceptionHandler(Exception e) {
        return createErrorReturnEntity(e, 404);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorReturnData> illegalArgumentExceptionHandler(Exception e) {
        return createErrorReturnEntity(e, 422);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ErrorReturnData> notAuthorizedExceptionHandler(Exception e) {
        return createErrorReturnEntity(e, 401);
    }
}
