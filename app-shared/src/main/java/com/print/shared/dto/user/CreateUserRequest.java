package com.print.shared.dto.user;

import com.print.shared.enums.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String name; //full name
    private String username;
    private String email;
    private String password;
    private String phone;
    private String gender;
    private String address;
    private Role role;
}
