package com.variate.dao;

import com.variate.model.Category;

import java.util.Optional;

public interface CategoryDao {
    void create(Category category);

    Optional<Category> findOne(long l);
}
