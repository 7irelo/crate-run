package com.variate.dao;

import com.variate.model.entities.Order;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    void create(Order order);
    Optional<Order> findOne(Long id);
    List<Order> find();
    void update(Long id, Order order);
    void patch(Long id, Float totalCost, String status);
    void delete(Long id);
}
