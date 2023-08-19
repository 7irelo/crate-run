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
public class OrderDto {

    public Long id;

    public String identityUserId;

    public LocalDateTime orderDateTime;

    public Float totalCost;

    public String status;
}
