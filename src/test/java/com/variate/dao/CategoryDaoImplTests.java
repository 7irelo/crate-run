package com.variate.dao;

import com.variate.dao.impl.CategoryDaoImpl;
import com.variate.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        Category category = Category.builder()
                .Id(1L)
                .Name("Electronics")
                .Description("Gadgets and consoles")
                .ImageUrl("electronics.jpg")
                .build();
        underTest.create(category);

        verify(jdbcTemplate).update(
                eq("INSERT INTO categories (id, name, description, image_url) VALUES (?, ?, ?, ?)"),
                eq(1L), eq("Electronics"), eq("Gadgets and consoles"), eq("electronics.jpg")
        );
    }
}
