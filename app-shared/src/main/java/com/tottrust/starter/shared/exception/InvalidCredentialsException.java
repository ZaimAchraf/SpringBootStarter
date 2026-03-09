package com.tottrust.starter.shared.exception;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
