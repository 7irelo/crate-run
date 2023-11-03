package com.variate.services.impl;

import com.variate.dao.OrderDao;
import com.variate.mappers.impl.OrderMapper;
import com.variate.model.dto.OrderDto;
import com.variate.model.entities.Order;
import com.variate.services.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderDao orderDao, OrderMapper orderMapper) {
        this.orderDao = orderDao;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = orderMapper.mapFrom(orderDto);
        return orderMapper.mapTo(orderDao.create(order));
    }

    @Override
    public OrderDto getOrderById(Long id) {
        return orderDao.findOne(id).map(orderMapper::mapTo).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return orderDao.find().stream().map(orderMapper::mapTo).collect(Collectors.toList());
    }

    @Override
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderMapper.mapFrom(orderDto);
        orderDao.update(id, order);
        return orderMapper.mapTo(order);
    }

    @Override
    public OrderDto patchOrder(Long id, OrderDto orderDto) {
        orderDao.patch(id, orderDto.getTotalCost(), orderDto.getStatus());
        return orderMapper.mapTo(orderDao.findOne(id).orElseThrow(() -> new RuntimeException("Order not found")));
    }

    @Override
    public void deleteOrder(Long id) {
        orderDao.delete(id);
    }
}
