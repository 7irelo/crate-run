package com.variate.dao;

import com.variate.model.entities.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    void create(User user);
    Optional<User> findOne(Long id);
    Optional<User> findByUsername(String username);
    List<User> find();
    void update(Long id, User user);
    void delete(Long id);
}
