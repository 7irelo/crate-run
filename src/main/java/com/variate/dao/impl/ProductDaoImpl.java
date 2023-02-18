package com.variate.dao.impl;

import com.variate.dao.ProductDao;
import com.variate.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;

public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Product product) {
        jdbcTemplate.update(
                "INSERT INTO categories (id, category_id, name, description, price, image_url, on_sale) VALUES (?, ?, ?, ?, ?, ?, ?)",
                product.getId(), product.getCategoryId(), product.getName(), product.getDescription(), product.getPrice(), product.getImageUrl(), product.getOnSale()
        );
    }
}
