package com.variate.dao;

import com.variate.dao.impl.ProductDaoImpl;
import com.variate.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        Product product = Product.builder()
                .Id(1L)
                .CategoryId(1L)
                .Name("IPhone 11 Pro Max")
                .Description("Deez")
                .Price(10000F)
                .ImageUrl("iphone_11_pro_max.jpg")
                .OnSale(false)
                .build();
        underTest.create(product);

        verify(jdbcTemplate).update(
                eq("INSERT INTO categories (id, category_id, name, description, price, image_url, on_sale) VALUES (?, ?, ?, ?, ?, ?, ?)"),
                eq(1L), eq(1L), eq("IPhone 11 Pro Max"), eq("Deez"), eq(10000F), eq("iphone_11_pro_max.jpg"), eq(false)
        );
    }
}
