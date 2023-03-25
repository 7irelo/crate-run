package com.variate.dao.impl;

import com.variate.TestDataUtil;
import com.variate.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CategoryDaoImplTests {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CategoryDaoImpl underTest;

    @Test
    public void testThatCreateCategoryGeneratesCorrectSql() {
        Category category = TestDataUtil.createTestCategoryA();
        underTest.create(category);

        verify(jdbcTemplate).update(
                eq("INSERT INTO categories (id, name, description, image_url) VALUES (?, ?, ?, ?)"),
                eq(1L), eq("Electronics"), eq("Gadgets and consoles"), eq("electronics.jpg")
        );
    }

    @Test
    public void testThatFindOneGeneratesCorrectSql() {
        underTest.findOne(1L);
        verify(jdbcTemplate).query(
                eq("SELECT id, name, description, image_url FROM categories WHERE id = ? LIMIT 1"),
                ArgumentMatchers.<CategoryDaoImpl.CategoryRowMapper>any(),
                eq(1L)
        );
    }

    @Test
    void testThatFindManyGeneratesCorrectSql() {
        underTest.find();
        verify(jdbcTemplate).query(
                eq("SELECT id, name, description, image_url FROM categories"),
                ArgumentMatchers.<CategoryDaoImpl.CategoryRowMapper>any()
        );
    }
}
