package com.variate.services;

import com.variate.model.dto.PaymentDto;

import java.util.List;

public interface PaymentService {

    // Create a new payment
    PaymentDto createPayment(PaymentDto paymentDto);

    // Get a single payment by ID
    PaymentDto getPaymentById(Long id);

    // Get all payments
    List<PaymentDto> getAllPayments();

    // Update a specific payment
    PaymentDto updatePayment(Long id, PaymentDto paymentDto);

    // Partial update of a payment
    PaymentDto patchPayment(Long id, PaymentDto paymentDto);

    // Delete a payment by ID
    void deletePayment(Long id);
}
