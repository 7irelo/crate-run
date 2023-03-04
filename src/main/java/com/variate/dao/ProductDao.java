package com.variate.dao;

import com.variate.model.Category;
import com.variate.model.Product;

import java.util.Optional;

public interface ProductDao {
    void create(Product product);

    Optional<Product> findOne(long l);
}
