package com.variate.services;

import com.variate.model.dto.ReviewDto;

import java.util.List;

public interface ReviewService {

    // Create a new review
    ReviewDto createReview(ReviewDto reviewDto);

    // Get a review by ID
    ReviewDto getReviewById(Long id);

    // Get all reviews
    List<ReviewDto> getAllReviews();

    // Update a review by ID (PUT)
    ReviewDto updateReview(Long id, ReviewDto reviewDto);

    // Partial update of a review by ID (PATCH)
    ReviewDto patchReview(Long id, ReviewDto reviewDto);

    // Delete a review by ID
    void deleteReview(Long id);
}
