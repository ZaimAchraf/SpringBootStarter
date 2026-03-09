package com.tottrust.starter.shared.exception;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String email) {
        super("A user with email " + email + " already exists");
    }
}
