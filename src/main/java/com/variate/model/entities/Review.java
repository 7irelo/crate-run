package com.variate.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Long id;
    private Long productId;
    private Long userId;
    private Integer rating;
    private String reviewComment;
}
