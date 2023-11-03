package com.variate.services;

import com.variate.model.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);
    OrderDto getOrderById(Long id);
    List<OrderDto> getAllOrders();
    OrderDto updateOrder(Long id, OrderDto orderDto);
    OrderDto patchOrder(Long id, OrderDto orderDto);
    void deleteOrder(Long id);
}
