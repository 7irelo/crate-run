package com.variate.services;

import com.variate.model.dto.OrderItemDto;

import java.util.List;

public interface OrderItemService {

    // Create a new order item
    OrderItemDto createOrderItem(OrderItemDto orderItemDto);

    // Get a single order item by ID
    OrderItemDto getOrderItemById(Long id);

    // Get all order items
    List<OrderItemDto> getAllOrderItems();

    // Update a specific order item
    OrderItemDto updateOrderItem(Long id, OrderItemDto orderItemDto);

    OrderItemDto patchOrderItem(Long id, OrderItemDto orderItemDto);

    // Delete an order item by ID
    void deleteOrderItem(Long id);
}
