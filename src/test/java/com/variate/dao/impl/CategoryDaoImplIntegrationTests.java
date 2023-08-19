package com.variate.dao.impl;

import com.variate.TestDataUtil;
import com.variate.model.entities.Category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CategoryDaoImplIntegrationTests {

    private CategoryDaoImpl underTest;


    @Autowired
    public CategoryDaoImplIntegrationTests(CategoryDaoImpl underTest) {
        this.underTest = underTest;
    }

    @Test
    public void testThatCategoryCanBeCreatedAndRecalled() {
        Category category = TestDataUtil.createTestCategoryA();
        underTest.create(category);
        Optional<Category> result = underTest.findOne(category.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(category);
    }

    @Test
    void testThatMultipleCategoriesCanBeCreatedAndRecalled() {
        Category categoryA = TestDataUtil.createTestCategoryA();
        underTest.create(categoryA);
        Category categoryB = TestDataUtil.createTestCategoryB();
        underTest.create(categoryB);
        Category categoryC = TestDataUtil.createTestCategoryC();
        underTest.create(categoryC);

        List<Category> result =  underTest.find();
        assertThat(result).hasSize(3).containsExactly(categoryA, categoryB, categoryC);
    }

    @Test
    void testThatCategoryCanBeUpdated() {
        Category category = TestDataUtil.createTestCategoryA();
        underTest.create(category);

        category.setName("UPDATED");
        underTest.update(category.getId(), category);

        Optional<Category> result = underTest.findOne(category.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(category);
    }

    @Test
    void testThatCategoryCanDeleted() {
        Category category = TestDataUtil.createTestCategoryA();
        underTest.create(category);
        underTest.delete(category.getId());
        Optional<Category>result = underTest.findOne(category.getId());
        assertThat(result).isEmpty();
    }
}
