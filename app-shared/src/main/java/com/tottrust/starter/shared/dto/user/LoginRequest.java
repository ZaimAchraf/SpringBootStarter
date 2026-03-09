package com.tottrust.starter.shared.dto.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
