package com.variate.services.impl;

import com.variate.mappers.impl.UserMapper;
import com.variate.model.dto.UserDto;
import com.variate.model.entities.User;
import com.variate.services.UserService;
import com.variate.dao.UserDao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    public UserServiceImpl(UserDao userDao, UserMapper userMapper) {
        this.userDao = userDao;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        var user = userMapper.mapFrom(userDto);
        user.setPassword(userMapper.encodePassword(user.getPassword()));
        return userMapper.mapTo(userDao.create(user));
    }

    @Override
    public UserDto getUserById(Long id) {
        return userDao.findById(id)
                .map(userMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDto getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .map(userMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
