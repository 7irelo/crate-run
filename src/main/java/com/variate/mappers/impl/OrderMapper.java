package com.variate.mappers.impl;

import com.variate.mappers.Mapper;
import com.variate.model.dto.OrderDto;
import com.variate.model.entities.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper implements Mapper<Order, OrderDto> {

    private final ModelMapper modelMapper;

    public OrderMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderDto mapTo(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

    @Override
    public Order mapFrom(OrderDto orderDto) {
        return modelMapper.map(orderDto, Order.class);
    }
}
