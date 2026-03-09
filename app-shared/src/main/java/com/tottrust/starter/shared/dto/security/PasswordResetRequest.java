package com.tottrust.starter.shared.dto.security;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
}
