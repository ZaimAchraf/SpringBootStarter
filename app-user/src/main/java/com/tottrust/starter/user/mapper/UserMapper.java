package com.tottrust.starter.user.mapper;

import com.tottrust.starter.shared.dto.user.UserDTO;
import com.tottrust.starter.user.entity.User;
import org.springframework.stereotype.Component;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setRole(String.valueOf(user.getRole()));
        return dto;
    }
}

