package com.variate.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    public Long Id;
    public Long CategoryId;
    public String Name;
    public String Description;
    public Float Price;
    public Date Release;
    public String ImageUrl;
    public Boolean OnSale;
}
