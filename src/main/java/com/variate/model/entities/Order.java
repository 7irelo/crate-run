package com.variate.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;
    private String userId;
    private LocalDateTime orderDateTime = LocalDateTime.now();
    private Float totalCost;
    private String status;
}
