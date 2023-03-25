package com.variate;

import com.variate.model.Category;
import com.variate.model.Product;

public final class TestDataUtil {
    private TestDataUtil() {

    }

    public static Category createTestCategoryA() {
        return Category.builder()
                .Id(1L)
                .Name("Electronics")
                .Description("Gadgets and consoles")
                .ImageUrl("electronics.jpg")
                .build();
    }

    public static Category createTestCategoryB() {
        return Category.builder()
                .Id(2L)
                .Name("Home and Kitchen")
                .Description("Home appliances and kitchenware")
                .ImageUrl("home_and_kitchen.jpg")
                .build();
    }

    public static Category createTestCategoryC() {
        return Category.builder()
                .Id(3L)
                .Name("Fashion and Beauty")
                .Description("Fashion and beauty products")
                .ImageUrl("fashion_and_beauty.jpg")
                .build();
    }

    public static Product createTestProductA() {
        return Product.builder()
                .Id(1L)
                .CategoryId(1L)
                .Name("Samsung A15 Blue")
                .Description("A sleek smartphone with a powerful processor.")
                .Price(5999F)
                .ImageUrl("samsung_a15_blue.jpg")
                .OnSale(true)
                .build();
    }

    public static Product createTestProductB() {
        return Product.builder()
                .Id(2L)
                .CategoryId(1L)
                .Name("Amazon Echo Show 10")
                .Description("Smart display with Alexa and a rotating screen.")
                .Price(8999F)
                .ImageUrl("amazon_echo_show_10.jpg")
                .OnSale(false)
                .build();
    }

    public static Product createTestProductC() {
        return Product.builder()
                .Id(3L)
                .CategoryId(1L)
                .Name("Bose SoundLink Revolve")
                .Description("Portable Bluetooth speaker with 360-degree sound.")
                .Price(2499F)
                .ImageUrl("bose_soundlink_revolve.jpg")
                .OnSale(true)
                .build();
    }
}
