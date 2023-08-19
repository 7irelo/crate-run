package com.variate.mappers.impl;

import com.variate.mappers.Mapper;
import com.variate.model.dto.OrderItemDto;
import com.variate.model.entities.OrderItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper implements Mapper<OrderItem, OrderItemDto> {

    private final ModelMapper modelMapper;

    public OrderItemMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderItemDto mapTo(OrderItem orderItem) {
        return modelMapper.map(orderItem, OrderItemDto.class);
    }

    @Override
    public OrderItem mapFrom(OrderItemDto orderItemDto) {
        return modelMapper.map(orderItemDto, OrderItem.class);
    }
}
