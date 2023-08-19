package com.variate.dao;

import com.variate.model.entities.OrderItem;
import java.util.List;
import java.util.Optional;

public interface OrderItemDao {
    void create(OrderItem orderItem);
    Optional<OrderItem> findOne(Long id);
    List<OrderItem> find();
    void update(Long id, OrderItem orderItem);
    void delete(Long id);
}
