package com.variate.services.impl;

import com.variate.dao.CategoryDao;
import com.variate.mappers.impl.CategoryMapper;
import com.variate.model.dto.CategoryDto;
import com.variate.model.entities.Category;
import com.variate.services.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryDao categoryDao, CategoryMapper categoryMapper) {
        this.categoryDao = categoryDao;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = categoryMapper.mapFrom(categoryDto);
        categoryDao.create(category);
        return categoryMapper.mapTo(category);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        return categoryDao.findOne(id)
                .map(categoryMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryDao.find().stream()
                .map(categoryMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryMapper.mapFrom(categoryDto);
        categoryDao.update(id, category);
        return categoryMapper.mapTo(categoryDao.findOne(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id)));
    }

    @Override
    public CategoryDto patchCategory(Long id, CategoryDto categoryDto) {
        categoryDao.patch(id, categoryDto.getName(), categoryDto.getDescription(), categoryDto.getImageUrl());
        return categoryDao.findOne(id)
                .map(categoryMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    @Override
    public void deleteCategory(Long id) {
        if (categoryDao.findOne(id).isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + id);
        }
        categoryDao.delete(id);
    }
}
