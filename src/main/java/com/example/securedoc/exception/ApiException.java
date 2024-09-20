package com.example.securedoc.exception;

public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException() {
        super("An error has occurred");
    }
}
