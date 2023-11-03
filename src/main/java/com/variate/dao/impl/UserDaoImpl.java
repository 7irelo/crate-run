package com.variate.dao.impl;

import com.variate.dao.UserDao;
import com.variate.model.entities.Role;
import com.variate.model.entities.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // Insert new user
            jdbcTemplate.update(
                    "INSERT INTO users (username, email, password) VALUES (?, ?, ?)",
                    user.getUsername(), user.getEmail(), user.getPassword()
            );
        } else {
            // Update existing user
            jdbcTemplate.update(
                    "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?",
                    user.getUsername(), user.getEmail(), user.getPassword(), user.getId()
            );
        }
        // Handle roles
        updateRoles(user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> results = jdbcTemplate.query(
                "SELECT * FROM users WHERE id = ?",
                new UserRowMapper(jdbcTemplate), id
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> results = jdbcTemplate.query(
                "SELECT * FROM users WHERE username = ?",
                new UserRowMapper(jdbcTemplate), username
        );
        return results.stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", new UserRowMapper(jdbcTemplate));
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        // Also delete user-role mappings
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class, id
        );
        return count != null && count > 0;
    }

    private void updateRoles(User user) {
        // Clear existing roles
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());

        // Insert new roles
        for (Role role : user.getRoles()) {
            jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                    user.getId(), role.getId()
            );
        }
    }

    private record UserRowMapper(JdbcTemplate jdbcTemplate) implements RowMapper<User> {

        @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                // Map the basic user details
                User user = User.builder()
                        .id(rs.getLong("id"))
                        .username(rs.getString("username"))
                        .email(rs.getString("email"))
                        .password(rs.getString("password"))
                        .build();

                // Fetch the roles associated with this user
                List<Role> roles = jdbcTemplate.query(
                        "SELECT r.id, r.name FROM roles r INNER JOIN user_roles ur ON ur.role_id = r.id WHERE ur.user_id = ?",
                        new RoleRowMapper(), user.getId()
                );

                user.setRoles(new HashSet<>(roles));
                return user;
            }
        }

    private static class RoleRowMapper implements RowMapper<Role> {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Role.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .build();
        }
    }
}
