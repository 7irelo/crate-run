package com.variate.services.impl;

import com.variate.dao.ProductDao;
import com.variate.mappers.impl.ProductMapper;
import com.variate.model.dto.ProductDto;
import com.variate.model.entities.Product;
import com.variate.services.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductDao productDao, ProductMapper productMapper) {
        this.productDao = productDao;
        this.productMapper = productMapper;
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.mapFrom(productDto);
        productDao.create(product);
        return productMapper.mapTo(product);
    }

    @Override
    public ProductDto getProductById(Long id) {
        return productDao.findOne(id)
                .map(productMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productDao.find().stream()
                .map(productMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productMapper.mapFrom(productDto);
        productDao.update(id, product);
        return productMapper.mapTo(product);
    }

    @Override
    public ProductDto patchProduct(Long id, ProductDto productDto) {
        productDao.patch(id, productDto.getName(), productDto.getPrice(), productDto.getImageUrl(), productDto.getOnSale());
        return productDao.findOne(id)
                .map(productMapper::mapTo)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        productDao.delete(id);
    }
}
