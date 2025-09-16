package com.example.orderup.service;

import com.example.orderup.controller.GlobalExceptionHandler;
import com.example.orderup.dto.OrderRequest;
import com.example.orderup.dto.OrderResponse;
import com.example.orderup.entity.Order;
import com.example.orderup.entity.Product;
import com.example.orderup.exception.InsufficientStockException;
import com.example.orderup.exception.OrderProcessingException;
import com.example.orderup.exception.ProductNotFoundException;
import com.example.orderup.repository.OrderRepository;
import com.example.orderup.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;

    @BeforeEach
    public void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setStock(10);
    }

    @Test
    void testPlaceOrderSuccess() {
        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setCustomerName("Alice");
        request.setQuantity(2);

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setProduct(product);
        savedOrder.setCustomerName("Alice");
        savedOrder.setQuantity(2);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
        assertEquals("Test Product", response.getProductName());
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

        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void testInsufficientStock() {
        product.setStock(1);

        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setCustomerName("TestUser");
        request.setQuantity(5);  // Exceeds stock

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void testGetOrderByIdSuccess() {
        Order order = new Order();
        order.setId(123L);
        order.setCustomerName("Customer X");
        order.setQuantity(3);
        order.setProduct(product);

        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(123L);

        assertNotNull(response);
        assertEquals(123L, response.getOrderId());
        assertEquals(3, response.getQuantity());
        assertEquals("Customer X", order.getCustomerName());
    }

    @Test
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(404L));
    }

    @Test
    void testGetAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomerName("Alice");
        order1.setQuantity(2);
        order1.setProduct(product);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerName("Bob");
        order2.setQuantity(1);
        order2.setProduct(product);

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        var responses = orderService.getAllOrders();

        assertEquals(2, responses.size());
        assertEquals(2, responses.get(0).getQuantity());
        assertEquals(1, responses.get(1).getQuantity());
    }


    @Test
    void testGetProductStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        int stock = orderService.getProductStock(1L);

        assertEquals(10, stock);
    }

    @Test
    void testGetProductStockNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getProductStock(999L));
    }
    @Test
    void testUnexpectedErrorDuringOrderPlacement() {
        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setCustomerName("Unexpected");
        request.setQuantity(1);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product");
        product.setStock(10);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> orderService.placeOrder(request));

        assertTrue(exception.getMessage().contains("Failed to process order"));
    }
    @Test
    void testGetOrderById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        Order order = Order.builder()
                .id(1L)
                .product(product)
                .quantity(3)
                .customerName("John")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertEquals(1L, response.getOrderId());
        assertEquals("Test Product", response.getProductName());
        assertEquals(3, response.getQuantity());
    }

    @Test
    void testOrderWithExactStock() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Exact Stock Product");
        product.setStock(3);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequest request = new OrderRequest();
        request.setProductId(1L);
        request.setCustomerName("ExactStockUser");
        request.setQuantity(3);  // ordering exactly what's in stock

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(3, response.getQuantity());
        assertEquals("Order placed successfully", response.getMessage());
        assertEquals("Exact Stock Product", response.getProductName());
    }

    @Test
    void testUnexpectedErrorDuringSave() {
        Product product = new Product();
        product.setId(1L);
        product.setName("ErrProduct");
        product.setStock(5);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("DB fail"));

        OrderRequest req = new OrderRequest();
        req.setProductId(1L);
        req.setCustomerName("User");
        req.setQuantity(1);

        Exception ex = assertThrows(OrderProcessingException.class, () -> orderService.placeOrder(req));
        assertTrue(ex.getMessage().contains("Failed to process order"));
    }


}
