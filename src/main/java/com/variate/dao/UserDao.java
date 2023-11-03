package com.variate.dao;

import com.variate.model.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    // Save a new user or update an existing user
    User save(User user);

    // Find a user by ID
    Optional<User> findById(Long id);

    // Find a user by username
    Optional<User> findByUsername(String username);

    // Find all users
    List<User> findAll();

    // Delete a user by ID
    void deleteById(Long id);

    // Check if a user exists by ID
    boolean existsById(Long id);
}
