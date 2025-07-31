package com.koundary.domain.verification.exception;

public class VerificationExpiredException extends RuntimeException {
    public VerificationExpiredException(String message) {
        super(message);
    }
}
