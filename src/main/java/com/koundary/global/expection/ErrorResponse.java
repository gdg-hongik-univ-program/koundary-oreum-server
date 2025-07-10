package com.koundary.global.expection;

public class ErrorResponse extends RuntimeException {
    public ErrorResponse(String message) {
        super(message);
    }
}
