package com.variate.mappers.impl;

import com.variate.mappers.Mapper;
import com.variate.model.dto.PaymentDto;
import com.variate.model.entities.Payment;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper implements Mapper<Payment, PaymentDto> {

    private final ModelMapper modelMapper;

    public PaymentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PaymentDto mapTo(Payment payment) {
        return modelMapper.map(payment, PaymentDto.class);
    }

    @Override
    public Payment mapFrom(PaymentDto paymentDto) {
        return modelMapper.map(paymentDto, Payment.class);
    }
}
