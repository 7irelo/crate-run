package com.variate.mappers.impl;

import com.variate.mappers.Mapper;
import com.variate.model.dto.ReviewDto;
import com.variate.model.entities.Review;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper implements Mapper<Review, ReviewDto> {

    private final ModelMapper modelMapper;

    public ReviewMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ReviewDto mapTo(Review review) {
        return modelMapper.map(review, ReviewDto.class);
    }

    @Override
    public Review mapFrom(ReviewDto reviewDto) {
        return modelMapper.map(reviewDto, Review.class);
    }
}
