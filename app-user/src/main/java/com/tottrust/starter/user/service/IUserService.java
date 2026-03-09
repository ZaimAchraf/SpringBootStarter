package com.tottrust.starter.user.service;

import com.tottrust.starter.shared.dto.user.CreateUserRequest;
import com.tottrust.starter.shared.dto.user.UserDTO;
import com.tottrust.starter.user.entity.User;

public interface IUserService {
    UserDTO createUser(CreateUserRequest request);
    User getUserByUsername(String username);
}
