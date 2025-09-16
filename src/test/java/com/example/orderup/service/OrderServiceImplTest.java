package com.example.orderup.service;

import com.example.orderup.dto.OrderRequest;
import com.example.orderup.dto.OrderResponse;
import com.example.orderup.entity.Order;
import com.example.orderup.entity.Product;
import com.example.orderup.exception.InsufficientStockException;
import com.example.orderup.exception.ProductNotFoundException;
import com.example.orderup.repository.OrderRepository;
import com.example.orderup.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;

    @BeforeEach
    public void setUpProduct() {
        Product product = productRepository.findById(1L).orElse(null);
        if (product == null) {
            product = new Product();
            product.setName("Test Product");
            product.setStock(10);
            product = productRepository.save(product);
        } else {
            product.setStock(10);
            productRepository.save(product);
        }
    }

    @Test
    void testPlaceOrderSuccess() {
        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setCustomerName("Alice");
        request.setQuantity(2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(2, response.getQuantity());
        assertEquals("Order placed successfully", response.getMessage());
        verify(productRepository).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testProductNotFound() {
        OrderRequest request = new OrderRequest();
        request.setProductId(99L);
        request.setCustomerName("Bob");
        request.setQuantity(1);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void testInsufficientStock() {
        // Prepare product with stock = 1
        Product product = new Product();
        product.setId(1L);
        product.setName("Low Stock Product");
        product.setStock(1);

        // Prepare order request with quantity > stock
        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setCustomerName("TestUser");
        request.setQuantity(5);  // Exceeds stock

        // Mock productRepository.findById to return product with low stock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Run the test and expect InsufficientStockException
        assertThrows(InsufficientStockException.class, () -> {
            orderService.placeOrder(request);
        });
    }
}
