package com.variate.dao.impl;

import com.variate.dao.OrderDao;
import com.variate.model.entities.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class OrderDaoImpl implements OrderDao {

    private final JdbcTemplate jdbcTemplate;

    public OrderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order create(Order order) {
        jdbcTemplate.update(
            "INSERT INTO orders (user_id, order_date_time, total_cost, status) VALUES (?, ?, ?, ?)",
            order.getUserId(), order.getOrderDateTime(), order.getTotalCost(), order.getStatus()
        );
        return order;
    }

    @Override
    public Optional<Order> findOne(Long id) {
        List<Order> results = jdbcTemplate.query(
            "SELECT * FROM orders WHERE id = ?",
            new OrderRowMapper(), id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Order> find() {
        return jdbcTemplate.query("SELECT * FROM orders", new OrderRowMapper());
    }

    @Override
    public void update(Long id, Order order) {
        jdbcTemplate.update(
            "UPDATE orders SET user_id = ?, order_date_time = ?, total_cost = ?, status = ? WHERE id = ?",
            order.getUserId(), order.getOrderDateTime(), order.getTotalCost(), order.getStatus(), id
        );
    }

    @Override
    public void patch(Long id, Float totalCost, String status) {
        jdbcTemplate.update(
            "UPDATE orders SET total_cost = COALESCE(?, total_cost), status = COALESCE(?, status) WHERE id = ?",
            totalCost, status, id
        );
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM orders WHERE id = ?", id);
    }

    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Order.builder()
                .id(rs.getLong("id"))
                .userId(rs.getString("user_id"))
                .orderDateTime(rs.getTimestamp("order_date_time").toLocalDateTime())
                .totalCost(rs.getFloat("total_cost"))
                .status(rs.getString("status"))
                .build();
        }
    }
}
