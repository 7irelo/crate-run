package com.variate;

import com.variate.model.Category;
import com.variate.model.Product;

public final class TestDataUtil {
    private TestDataUtil() {

    }

    public static Category createTestCategory() {
        return Category.builder()
                .Id(1L)
                .Name("Electronics")
                .Description("Gadgets and consoles")
                .ImageUrl("electronics.jpg")
                .build();
    }

    public static Product createTestProduct() {
        return Product.builder()
                .Id(1L)
                .CategoryId(1L)
                .Name("IPhone 11 Pro Max")
                .Description("Deez")
                .Price(10000F)
                .ImageUrl("iphone_11_pro_max.jpg")
                .OnSale(false)
                .build();
    }
}
