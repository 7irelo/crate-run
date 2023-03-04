package com.variate.dao.impl;

import com.variate.TestDataUtil;
import com.variate.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CategoryDaoImplIntergrationTests {

    private CategoryDaoImpl underTest;

    @Autowired
    public CategoryDaoImplIntergrationTests(CategoryDaoImpl underTest) {
        this.underTest = underTest;
    }

    @Test
    public void testThatCategoryCanBeCreatedAndRecalled() {
        Category category = TestDataUtil.createTestCategory();
        underTest.create(category);
        Optional<Category> result = underTest.findOne(category.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(category);
    }
}
