package com.variate.services;

import com.variate.model.dto.UserDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    // Create a new user
    UserDto createUser(UserDto userDto);

    // Read a single user by ID
    UserDto getUserById(Long id);

    // Read a single user by username
    UserDto getUserByUsername(java.lang.String username);

    // Read all users
    List<UserDto> getAllUsers();

    // Update a user (PUT)
    UserDto updateUser(Long id, UserDto userDto);

    // Partial update of a user (PATCH)
    UserDto patchUser(Long id, UserDto userDto);

    // Delete a user by ID
    void deleteUser(Long id);

    String verify(UserDto userDto, Authentication authentication);
}
