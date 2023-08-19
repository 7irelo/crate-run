package com.variate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    public Long id;

    public Long orderId;

    public String paymentMethod;

    public String paymentDate;

    public Float amount;
}
