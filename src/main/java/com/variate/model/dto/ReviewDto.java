package com.variate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    public Long id;

    public Long productId;

    public String identityUserId;

    public Integer rating;

    public String reviewComment;
}
