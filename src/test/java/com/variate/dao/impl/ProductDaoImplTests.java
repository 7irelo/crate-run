package com.variate.dao.impl;

import com.variate.TestDataUtil;
import com.variate.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.ParseException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductDaoImplTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ProductDaoImpl underTest;

    @Test
    public void testThatCreateProductGeneratesCorrectSql() throws ParseException {

        Product product = TestDataUtil.createTestProduct();
        underTest.create(product);

        verify(jdbcTemplate).update(
                eq("INSERT INTO products (id, category_id, name, description, price, image_url, on_sale) VALUES (?, ?, ?, ?, ?, ?, ?)"),
                eq(1L), eq(1L), eq("IPhone 11 Pro Max"), eq("Deez"), eq(10000F), eq("iphone_11_pro_max.jpg"), eq(false)
        );
    }

    @Test
    public void testThatFindOneGeneratesCorrectSql() {
        underTest.findOne(1L);
        verify(jdbcTemplate).query(
                eq("SELECT id, category_id, name, description, price, image_url, on_sale FROM products WHERE id = ? LIMIT 1"),
                ArgumentMatchers.<ProductDaoImpl.ProductRowMapper>any(),
                eq(1L)
        );
    }
}
