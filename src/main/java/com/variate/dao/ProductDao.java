package com.variate.dao;

import com.variate.model.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
    void create(Product product);
    Optional<Product> findOne(long productId);
    List<Product> find();
    void update(Long id, Product product);
    void patch(Long id, String name, Float price, String imageUrl, Boolean onSale); // Add this method
    void delete(long id);
}

