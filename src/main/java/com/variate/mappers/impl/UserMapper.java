package com.variate.mappers.impl;

import com.variate.mappers.Mapper;
import com.variate.model.dto.UserDto;
import com.variate.model.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserMapper(ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto mapTo(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User mapFrom(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
