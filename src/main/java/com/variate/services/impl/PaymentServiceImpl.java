package com.variate.services.impl;

import com.variate.dao.PaymentDao;
import com.variate.mappers.impl.PaymentMapper;
import com.variate.model.dto.PaymentDto;
import com.variate.model.entities.Payment;
import com.variate.services.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentDao paymentDao, PaymentMapper paymentMapper) {
        this.paymentDao = paymentDao;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Payment payment = paymentMapper.mapFrom(paymentDto);
        return paymentMapper.mapTo(paymentDao.create(payment));
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        return paymentDao.findOne(id)
                .map(paymentMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentDao.find().stream()
                .map(paymentMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto updatePayment(Long id, PaymentDto paymentDto) {
        Payment payment = paymentMapper.mapFrom(paymentDto);
        paymentDao.update(id, payment);
        return paymentMapper.mapTo(payment);
    }

    @Override
    public PaymentDto patchPayment(Long id, PaymentDto paymentDto) {
        paymentDao.patch(id, paymentDto.getAmount(), paymentDto.getPaymentMethod());
        return getPaymentById(id);
    }

    @Override
    public void deletePayment(Long id) {
        paymentDao.delete(id);
    }
}
