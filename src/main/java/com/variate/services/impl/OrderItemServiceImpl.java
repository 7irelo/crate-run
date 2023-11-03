package com.variate.services.impl;

import com.variate.dao.OrderItemDao;
import com.variate.mappers.impl.OrderItemMapper;
import com.variate.model.dto.OrderItemDto;
import com.variate.model.entities.OrderItem;
import com.variate.services.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemDao orderItemDao;
    private final OrderItemMapper orderItemMapper;

    public OrderItemServiceImpl(OrderItemDao orderItemDao, OrderItemMapper orderItemMapper) {
        this.orderItemDao = orderItemDao;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public OrderItemDto createOrderItem(OrderItemDto orderItemDto) {
        OrderItem orderItem = orderItemMapper.mapFrom(orderItemDto);
        return orderItemMapper.mapTo(orderItemDao.create(orderItem));
    }

    @Override
    public OrderItemDto getOrderItemById(Long id) {
        return orderItemDao.findOne(id)
                .map(orderItemMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Order item not found with ID: " + id));
    }

    @Override
    public List<OrderItemDto> getAllOrderItems() {
        return orderItemDao.find().stream()
                .map(orderItemMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDto updateOrderItem(Long id, OrderItemDto orderItemDto) {
        OrderItem orderItem = orderItemMapper.mapFrom(orderItemDto);
        orderItemDao.update(id, orderItem);
        return orderItemMapper.mapTo(orderItem);
    }

    @Override
    public OrderItemDto patchOrderItem(Long id, OrderItemDto orderItemDto) {
        orderItemDao.patch(id, orderItemDto.getQuantity(), orderItemDto.getUnitPrice());
        return orderItemDao.findOne(id)
                .map(orderItemMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Order item not found with ID: " + id));
    }

    @Override
    public void deleteOrderItem(Long id) {
        orderItemDao.delete(id);
    }
}
