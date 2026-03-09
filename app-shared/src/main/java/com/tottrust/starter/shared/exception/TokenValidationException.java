package com.tottrust.starter.shared.exception;

public class TokenValidationException extends BusinessException {
    public TokenValidationException(String message) {
        super("Invalid or expired JWT token: " + message);
    }
}
