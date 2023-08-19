package com.variate.dao;

import com.variate.model.entities.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    void create(Review review);
    Optional<Review> findOne(Long id);
    List<Review> find();
    void update(Long id, Review review);
    void delete(Long id);
}
