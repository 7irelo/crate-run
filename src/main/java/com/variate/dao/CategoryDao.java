package com.variate.dao;

import com.variate.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    void create(Category category);

    Optional<Category> findOne(long l);

    List<Category> find();

    void update(long id, Category category);

    void delete(long id);
}
