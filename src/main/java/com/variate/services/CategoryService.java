package com.variate.services;

import com.variate.model.dto.CategoryDto;
import java.util.List;

public interface CategoryService {

    // Create a new category
    CategoryDto createCategory(CategoryDto categoryDto);

    // Get a category by ID
    CategoryDto getCategoryById(Long id);

    // Get all categories
    List<CategoryDto> getAllCategories();

    // Update a category by ID (PUT)
    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    // Partially update a category by ID (PATCH)
    CategoryDto patchCategory(Long id, CategoryDto categoryDto);

    // Delete a category by ID
    void deleteCategory(Long id);
}
