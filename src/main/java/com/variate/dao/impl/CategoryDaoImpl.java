package com.variate.dao.impl;

import com.variate.dao.CategoryDao;
import com.variate.model.entities.Category;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class CategoryDaoImpl implements CategoryDao {
    private final JdbcTemplate jdbcTemplate;

    public CategoryDaoImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Category category) {
        jdbcTemplate.update(
                "INSERT INTO categories (id, name, description, image_url) VALUES (?, ?, ?, ?)",
                category.getId(), category.getName(), category.getDescription(), category.getImageUrl()
        );
    }

    @Override
    public Optional<Category> findOne(long categoryId) {
        List<Category> results = jdbcTemplate.query(
                "SELECT id, name, description, image_url FROM categories WHERE id = ? LIMIT 1",
                new CategoryRowMapper(), categoryId
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Category> find() {
        return jdbcTemplate.query(
                "SELECT id, name, description, image_url FROM categories",
                new CategoryRowMapper()
        );
    }

    @Override
    public void update(long id, Category category) {
        jdbcTemplate.update(
                "UPDATE categories SET name = ?, description = ?, image_url = ? WHERE id = ?",
                category.getName(), category.getDescription(), category.getImageUrl(), id
        );
    }

    public void patch(long id, String name, String description, String imageUrl) {
        jdbcTemplate.update(
                "UPDATE categories SET name = COALESCE(?, name), description = COALESCE(?, description), image_url = COALESCE(?, image_url) WHERE id = ?",
                name, description, imageUrl, id
        );
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(
                "DELETE FROM categories WHERE id = ?",
                id
        );
    }

    public static class CategoryRowMapper implements RowMapper<Category> {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Category.builder()
                    .Id(rs.getLong("id"))
                    .Name(rs.getString("name"))
                    .Description(rs.getString("description"))
                    .ImageUrl(rs.getString("image_url"))
                    .build();
        }
    }
}
