package com.variate.dao.impl;

import com.variate.dao.ProductDao;
import com.variate.model.entities.Product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Product product) {
        jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, price, image_url, on_sale) VALUES (?, ?, ?, ?, ?, ?, ?)",
                product.getId(), product.getCategoryId(), product.getName(), product.getDescription(), product.getPrice(), product.getImageUrl(), product.getOnSale()
        );
    }

    @Override
    public Optional<Product> findOne(long productId) {
        List<Product> results = jdbcTemplate.query(
                "SELECT id, category_id, name, description, price, image_url, on_sale FROM products WHERE id = ? LIMIT 1",
                new ProductRowMapper(), productId
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Product> find() {
        return jdbcTemplate.query(
                "SELECT id, category_id, name, description, price, image_url, on_sale FROM products",
                new ProductRowMapper()
        );
    }

    @Override
    public void update(Long id, Product product) {
        jdbcTemplate.update(
                "UPDATE products SET name = ?, category_id = ?, description = ?, price = ?, image_url = ?, on_sale = ? WHERE id = ?",
                product.getName(), product.getCategoryId(), product.getDescription(), product.getPrice(), product.getImageUrl(), product.getOnSale(), id
        );
    }

    @Override
    public void patch(Long id, String name, Float price, String imageUrl, Boolean onSale) {
        StringBuilder query = new StringBuilder("UPDATE products SET ");
        boolean addComma = false;

        if (name != null) {
            query.append("name = ?");
            addComma = true;
        }

        if (price != null) {
            if (addComma) query.append(", ");
            query.append("price = ?");
            addComma = true;
        }

        if (imageUrl != null) {
            if (addComma) query.append(", ");
            query.append("image_url = ?");
            addComma = true;
        }

        if (onSale != null) {
            if (addComma) query.append(", ");
            query.append("on_sale = ?");
        }

        query.append(" WHERE id = ?");

        // Prepare arguments dynamically
        Object[] args;
        if (name != null && price != null && imageUrl != null && onSale != null) {
            args = new Object[] { name, price, imageUrl, onSale, id };
        } else {
            args = new Object[]{name,
                    price,
                    imageUrl,
                    onSale, id};
        }

        jdbcTemplate.update(query.toString(), args);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(
                "DELETE FROM products WHERE id = ?",
                id
        );
    }

    public static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Product.builder()
                    .Id(rs.getLong("id"))
                    .CategoryId(rs.getLong("category_id"))
                    .Name(rs.getString("name"))
                    .Description(rs.getString("description"))
                    .Price(rs.getFloat("price"))
                    .ImageUrl(rs.getString("image_url"))
                    .OnSale(rs.getBoolean("on_sale"))
                    .build();
        }
    }
}
