package com.variate.services.impl;

import com.variate.mappers.impl.UserMapper;
import com.variate.model.dto.UserDto;
import com.variate.model.entities.User;
import com.variate.dao.UserDao;
import com.variate.services.JwtService;
import com.variate.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserDao userDao;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public UserServiceImpl(UserDao userDao, JwtService jwtService, UserMapper userMapper) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    // Create User
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.mapFrom(userDto);
        user.setPassword(userMapper.encodePassword(user.getPassword()));
        return userMapper.mapTo(userDao.save(user));
    }

    // Login User (authenticationManager is passed in here)
    @Override
    public String verify(UserDto userDto, Authentication authentication) {
        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateToken(userDetails.getUsername());
        }
        return "Failed";
    }

    // Read One User by ID
    @Override
    public UserDto getUserById(Long id) {
        return userDao.findById(id)
                .map(userMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    // Read One User by Username
    @Override
    public UserDto getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .map(userMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    // Load UserDetails for Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    // Read Many Users
    @Override
    public List<UserDto> getAllUsers() {
        return userDao.findAll().stream()
                .map(userMapper::mapTo)
                .collect(Collectors.toList());
    }

    // Update (PUT) User
    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Update fields entirely
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(userMapper.encodePassword(userDto.getPassword()));
        }

        return userMapper.mapTo(userDao.save(existingUser));
    }

    // Partial Update (PATCH) User
    @Override
    public UserDto patchUser(Long id, UserDto userDto) {
        User user = userMapper.mapFrom(userDto);
        User existingUser = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Update only provided fields
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(userMapper.encodePassword(user.getPassword()));
        }

        return userMapper.mapTo(userDao.save(existingUser));
    }

    // Delete User
    @Override
    public void deleteUser(Long id) {
        if (!userDao.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        userDao.deleteById(id);
    }
}
