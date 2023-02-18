package com.variate.dao.impl;

import com.variate.dao.CategoryDao;
import com.variate.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;

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
}
