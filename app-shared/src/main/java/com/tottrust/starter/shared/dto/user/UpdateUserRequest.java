package com.tottrust.starter.shared.dto.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private String role;
    private String password;
}