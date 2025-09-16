package com.example.orderup.service;

import com.example.orderup.dto.ProductRequest;
import com.example.orderup.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    public int getProductStock(Long productId);
}
