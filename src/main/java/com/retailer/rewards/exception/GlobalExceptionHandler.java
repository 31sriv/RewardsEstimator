package com.retailer.rewards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCustomerNotFound(CustomerNotFoundException ex, WebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Customer Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTransactionNotFound(TransactionNotFoundException ex, WebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Transaction Not Found", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorDto> buildError(HttpStatus status, String error, String message, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorDto err = new ErrorDto(status.value(), error, message, path);
        return new ResponseEntity<>(err, status);
    }

}
