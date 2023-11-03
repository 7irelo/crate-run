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
    public Payment create(Payment payment) {
        jdbcTemplate.update(
            "INSERT INTO payments (order_id, payment_method, payment_date, amount) VALUES (?, ?, ?, ?)",
            payment.getOrderId(), payment.getPaymentMethod(), payment.getPaymentDate(), payment.getAmount()
        );
        return payment;
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
            payment.getOrderId(), payment.getPaymentMethod(), payment.getPaymentDate(), payment.getAmount(), id
        );
    }

    @Override
    public void patch(Long id, Float amount, String paymentMethod) {
        StringBuilder query = new StringBuilder("UPDATE payments SET ");
        boolean addComma = false;

        if (amount != null) {
            query.append("amount = ?");
            addComma = true;
        }

        if (paymentMethod != null) {
            if (addComma) {
                query.append(", ");
            }
            query.append("payment_method = ?");
        }

        query.append(" WHERE id = ?");

        // Prepare arguments dynamically
        Object[] args;
        if (amount != null && paymentMethod != null) {
            args = new Object[] { amount, paymentMethod, id };
        } else if (amount != null) {
            args = new Object[] { amount, id };
        } else {
            args = new Object[] { paymentMethod, id };
        }

        jdbcTemplate.update(query.toString(), args);
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
