package com.variate.services;

import com.variate.model.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserById(Long id);
    UserDto getUserByUsername(String username);
    void deleteUser(Long id);
}
