package com.variate.services.impl;

import com.variate.dao.ReviewDao;
import com.variate.mappers.impl.ReviewMapper;
import com.variate.model.dto.ReviewDto;
import com.variate.model.entities.Review;
import com.variate.services.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewDao reviewDao, ReviewMapper reviewMapper) {
        this.reviewDao = reviewDao;
        this.reviewMapper = reviewMapper;
    }

    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        Review review = reviewMapper.mapFrom(reviewDto);
        reviewDao.create(review);
        return reviewMapper.mapTo(review);
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        return reviewDao.findOne(id)
                .map(reviewMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewDao.find().stream()
                .map(reviewMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        Review review = reviewMapper.mapFrom(reviewDto);
        reviewDao.update(id, review);
        return reviewMapper.mapTo(review);
    }

    @Override
    public ReviewDto patchReview(Long id, ReviewDto reviewDto) {
        reviewDao.update(id, reviewMapper.mapFrom(reviewDto));
        return reviewDao.findOne(id)
                .map(reviewMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));
    }

    @Override
    public void deleteReview(Long id) {
        reviewDao.delete(id);
    }
}
