package com.variate;

import com.variate.model.Category;

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
}
