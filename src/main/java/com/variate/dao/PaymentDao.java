package com.variate.dao;

import com.variate.model.entities.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    void create(Payment payment);
    Optional<Payment> findOne(Long id);
    List<Payment> find();
    void update(Long id, Payment payment);
    void delete(Long id);
}
