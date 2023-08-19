package com.variate.dao;

import com.variate.model.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
    void create(Product product);

    Optional<Product> findOne(long l);

    List<Product> find();

    void update(Long id, Product product);

    void delete(long id);
}
