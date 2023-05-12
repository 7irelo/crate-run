package com.variate.dao.impl;

import com.variate.TestDataUtil;
import com.variate.dao.CategoryDao;
import com.variate.model.Category;
import com.variate.model.Product;
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
public class ProductDaoImplIntegrationTests {

    private CategoryDao categoryDao;
    private ProductDaoImpl underTest;

    @Autowired
    public ProductDaoImplIntegrationTests(ProductDaoImpl underTest, CategoryDao categoryDao) {
        this.underTest = underTest;
        this.categoryDao = categoryDao;
    }

    @Test
    void testThatProductCanBeCreatedAndRecalled() {
        Category category = TestDataUtil.createTestCategoryA();
        categoryDao.create(category);
        Product product = TestDataUtil.createTestProductA();
        product.setCategoryId(category.getId());
        underTest.create(product);
        Optional<Product> result = underTest.findOne(product.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);
    }

    @Test
    void testThatMultipleProductsCanBeCreatedAndRecalled() {
        Category category = TestDataUtil.createTestCategoryA();
        categoryDao.create(category);

        Product productA = TestDataUtil.createTestProductA();
        productA.setCategoryId(category.getId());
        underTest.create(productA);

        Product productB = TestDataUtil.createTestProductB();
        productB.setCategoryId(category.getId());
        underTest.create(productB);

        Product productC = TestDataUtil.createTestProductC();
        productC.setCategoryId(category.getId());
        underTest.create(productC);

        List<Product> result = underTest.find();
        assertThat(result).hasSize(3).containsExactly(productA, productB, productC);
    }

    @Test
    void testThatProductCanBeUpdated() {
        Category category = TestDataUtil.createTestCategoryA();
        categoryDao.create(category);

        Product product = TestDataUtil.createTestProductA();
        product.setCategoryId(category.getId());
        underTest.create(product);

        product.setName("UPDATED");
        underTest.update(product.getId(), product);

        Optional<Product> result = underTest.findOne(product.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);
    }

    @Test
    void testThatProductCanBeDeleted() {
        Category category = TestDataUtil.createTestCategoryA();
        categoryDao.create(category);

        Product product = TestDataUtil.createTestProductA();
        product.setCategoryId(category.getId());
        underTest.create(product);

        underTest.delete(product.getId());
        Optional<Product> result = underTest.findOne(product.getId());
        assertThat(result).isEmpty();
    }
}
