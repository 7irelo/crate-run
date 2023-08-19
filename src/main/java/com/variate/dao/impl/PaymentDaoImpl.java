package com.variate.dao.impl;

import com.variate.dao.PaymentDao;
import com.variate.model.entities.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class PaymentDaoImpl implements PaymentDao {

    private final JdbcTemplate jdbcTemplate;

    public PaymentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Payment payment) {
        jdbcTemplate.update(
            "INSERT INTO payments (order_id, payment_method, payment_date, amount) VALUES (?, ?, ?, ?)",
            payment.getOrder().getId(), payment.getPaymentMethod(), payment.getPaymentDate(), payment.getAmount()
        );
    }

    @Override
    public Optional<Payment> findOne(Long id) {
        List<Payment> results = jdbcTemplate.query(
            "SELECT * FROM payments WHERE id = ?",
            new PaymentRowMapper(), id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Payment> find() {
        return jdbcTemplate.query("SELECT * FROM payments", new PaymentRowMapper());
    }

    @Override
    public void update(Long id, Payment payment) {
        jdbcTemplate.update(
            "UPDATE payments SET order_id = ?, payment_method = ?, payment_date = ?, amount = ? WHERE id = ?",
            payment.getOrder().getId(), payment.getPaymentMethod(), payment.getPaymentDate(), payment.getAmount(), id
        );
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM payments WHERE id = ?", id);
    }

    private static class PaymentRowMapper implements RowMapper<Payment> {
        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Payment.builder()
                .id(rs.getLong("id"))
                .paymentMethod(rs.getString("payment_method"))
                .paymentDate(rs.getString("payment_date"))
                .amount(rs.getFloat("amount"))
                .build();
        }
    }
}
