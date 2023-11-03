package com.variate.services;

import com.variate.model.dto.ProductDto;

import java.util.List;

public interface ProductService {

    // Create a new product
    ProductDto createProduct(ProductDto productDto);

    // Get a single product by ID
    ProductDto getProductById(Long id);

    // Get all products
    List<ProductDto> getAllProducts();

    // Update a product by ID (PUT)
    ProductDto updateProduct(Long id, ProductDto productDto);

    // Partial update of a product by ID (PATCH)
    ProductDto patchProduct(Long id, ProductDto productDto);

    // Delete a product by ID
    void deleteProduct(Long id);
}
