package com.tottrust.starter.shared.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String gender;
    private String role;
}
