package com.print.user.service;

import com.print.shared.dto.user.CreateUserRequest;
import com.print.shared.dto.user.UserDTO;
import com.print.user.entity.User;

public interface IUserService {
    UserDTO createUser(CreateUserRequest request);
    User getUserByUsername(String username);
}
