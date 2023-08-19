package com.variate.dao;

import java.util.List;
import java.util.Optional;

import com.variate.model.entities.Category;

public interface CategoryDao {
    void create(Category category);

    Optional<Category> findOne(long l);

    List<Category> find();

    void update(long id, Category category);

    void delete(long id);
}
