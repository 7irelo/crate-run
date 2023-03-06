package com.variate.dao.impl;

import com.variate.TestDataUtil;
import com.variate.dao.CategoryDao;
import com.variate.model.Category;
import com.variate.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ProductDaoImplIntergrationTests {

    private CategoryDao categoryDao;
    private ProductDaoImpl undertest;

    @Autowired
    public ProductDaoImplIntergrationTests(ProductDaoImpl undertest, CategoryDao categoryDao) {
        this.undertest = undertest;
        this.categoryDao = categoryDao;
    }

    @Test
    void testThatProductCanBeCreatedAndRecalled() {
        Category category = TestDataUtil.createTestCategory();
        categoryDao.create(category);
        Product product = TestDataUtil.createTestProduct();
        product.setCategoryId(category.getId());
        undertest.create(product);
        Optional<Product> result = undertest.findOne(product.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);
    }
}
