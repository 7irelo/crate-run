package com.variate.dao.impl;

import com.variate.dao.ProductDao;
import com.variate.model.Category;
import com.variate.model.Product;
import org.springframework.boot.actuate.endpoint.Producible;
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
                "UPDATE products SET id = ?, category_id = ?, name = ?, description = ?, price = ?, image_url = ?, on_sale = ? WHERE id = ?",
                product.getId(), product.getCategoryId(), product.getName(), product.getDescription(), product.getPrice(), product.getImageUrl(), product.getOnSale(), id
        );
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(
                "DELETE FROM products WHERE id = ?"
                , id
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
