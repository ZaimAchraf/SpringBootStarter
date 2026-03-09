package com.tottrust.starter.shared.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }
}
