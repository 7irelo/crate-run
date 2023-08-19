package com.variate.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    public Long Id;
    public String Name;
    public String Description;
    public String ImageUrl;
    public List<Product> Products;
}
