package com.variate.dao.impl;

import com.variate.dao.OrderItemDao;
import com.variate.model.entities.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class OrderItemDaoImpl implements OrderItemDao {

    private final JdbcTemplate jdbcTemplate;

    public OrderItemDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        jdbcTemplate.update(
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
            orderItem.getOrderId(), orderItem.getProductId(), orderItem.getQuantity(), orderItem.getUnitPrice()
        );
        return orderItem;
    }

    @Override
    public Optional<OrderItem> findOne(Long id) {
        List<OrderItem> results = jdbcTemplate.query(
            "SELECT * FROM order_items WHERE id = ?",
            new OrderItemRowMapper(), id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<OrderItem> find() {
        return jdbcTemplate.query("SELECT * FROM order_items", new OrderItemRowMapper());
    }

    @Override
    public void update(Long id, OrderItem orderItem) {
        jdbcTemplate.update(
            "UPDATE order_items SET order_id = ?, product_id = ?, quantity = ?, unit_price = ? WHERE id = ?",
            orderItem.getOrderId(), orderItem.getProductId(), orderItem.getQuantity(), orderItem.getUnitPrice(), id
        );
    }

    @Override
    public void patch(Long id, Integer quantity, Integer unitPrice) {
        StringBuilder query = new StringBuilder("UPDATE order_items SET ");
        boolean addComma = false;

        if (quantity != null) {
            query.append("quantity = ?");
            addComma = true;
        }

        if (unitPrice != null) {
            if (addComma) query.append(", ");
            query.append("unit_price = ?");
        }

        query.append(" WHERE id = ?");

        // Dynamically pass the non-null fields and the id to update
        Object[] args;
        if (quantity != null && unitPrice != null) {
            args = new Object[] { quantity, unitPrice, id };
        } else if (quantity != null) {
            args = new Object[] { quantity, id };
        } else {
            args = new Object[] { unitPrice, id };
        }

        jdbcTemplate.update(query.toString(), args);
    }


    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM order_items WHERE id = ?", id);
    }

    private static class OrderItemRowMapper implements RowMapper<OrderItem> {
        @Override
        public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return OrderItem.builder()
                .id(rs.getLong("id"))
                .quantity(rs.getInt("quantity"))
                .unitPrice(rs.getInt("unit_price"))
                .build();
        }
    }
}
