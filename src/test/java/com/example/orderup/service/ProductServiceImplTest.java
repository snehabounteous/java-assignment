package com.example.orderup.service;

import com.example.orderup.dto.ProductRequest;
import com.example.orderup.dto.ProductResponse;
import com.example.orderup.entity.Product;
import com.example.orderup.exception.ProductNotFoundException;
import com.example.orderup.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .stock(10)
                .build();
    }

    @Test
    void testCreateProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setStock(10);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
        assertEquals(product.getStock(), response.getStock());
        assertEquals("Product created successfully", response.getMessage());
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getName());
        assertEquals(10, response.getStock());
        assertEquals("Product retrieved successfully", response.getMessage());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(99L));
        assertEquals("Product not found with ID: 99", ex.getMessage());
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = List.of(product);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponse> responses = productService.getAllProducts();

        assertEquals(1, responses.size());
        ProductResponse response = responses.get(0);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
        assertEquals(product.getStock(), response.getStock());
        assertEquals("Product retrieved successfully", response.getMessage());
    }

    @Test
    void testUpdateProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setStock(15);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals(1L, response.getId());
        assertEquals("Updated Product", response.getName());
        assertEquals(15, response.getStock());
        assertEquals("Product updated successfully", response.getMessage());

        // Verify the product was updated before save
        assertEquals("Updated Product", product.getName());
        assertEquals(15, product.getStock());
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductRequest request = new ProductRequest();
        request.setName("Updated");
        request.setStock(10);

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(1L, request));
        assertEquals("Product not found with ID: 1", ex.getMessage());
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).delete(product);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(1L));
        assertEquals("Product not found with ID: 1", ex.getMessage());
    }

    @Test
    void testGetProductStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        int stock = productService.getProductStock(1L);
        assertEquals(10, stock);
    }

    @Test
    void testGetProductStock_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.getProductStock(1L));
        assertEquals("Product not found with ID: 1", ex.getMessage());
    }
}
