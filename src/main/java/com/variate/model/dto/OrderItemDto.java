package com.variate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    public Long id;

    public Long orderId;

    public Long productId;

    public Integer quantity;

    public Integer unitPrice;
}
