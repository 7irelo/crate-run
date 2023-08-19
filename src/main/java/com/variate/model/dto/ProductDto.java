package com.variate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    public Long id;

    public String name;

    public String description;

    public Float price;

    public LocalDateTime manufactured;

    public Boolean onSale;

    public String imageUrl;

    public Long categoryId;
}
