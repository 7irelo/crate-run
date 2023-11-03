package com.variate.dao;

import com.variate.model.entities.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    Payment create(Payment payment);
    Optional<Payment> findOne(Long id);
    List<Payment> find();
    void update(Long id, Payment payment);
    void patch(Long id, Float amount, String paymentMethod); // Add this method
    void delete(Long id);
}

