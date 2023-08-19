package com.variate.dao.impl;

import com.variate.dao.UserDao;
import com.variate.model.entities.User;
import com.variate.model.entities.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(User user) {
        jdbcTemplate.update(
            "INSERT INTO users (username, password) VALUES (?, ?)",
            user.getUsername(), user.getPassword()
        );
        // Handle roles association
        updateRoles(user);
    }

    @Override
    public Optional<User> findOne(Long id) {
        List<User> results = jdbcTemplate.query(
            "SELECT * FROM users WHERE id = ?",
            new UserRowMapper(), id
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> results = jdbcTemplate.query(
            "SELECT * FROM users WHERE username = ?",
            new UserRowMapper(), username
        );
        return results.stream().findFirst();
    }

    @Override
    public List<User> find() {
        return jdbcTemplate.query("SELECT * FROM users", new UserRowMapper());
    }

    @Override
    public void update(Long id, User user) {
        jdbcTemplate.update(
            "UPDATE users SET username = ?, password = ? WHERE id = ?",
            user.getUsername(), user.getPassword(), id
        );
        // Handle roles update
        updateRoles(user);
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        // Also delete user-role mappings
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", id);
    }

    private void updateRoles(User user) {
        // Clear existing roles
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());

        // Insert new roles
        user.getRoles().forEach(role -> {
            jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                user.getId(), role.getId()
            );
        });
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Fetch user details
            User user = User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .build();

            // Fetch roles for the user
            // Set<Role> roles = new HashSet<>(rs.getArray("roles"));
            // user.setRoles(roles);

            return user;
        }
    }
}
