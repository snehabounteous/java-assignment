package com.example.orderup.controller;

import com.example.orderup.dto.ErrorResponse;
import com.example.orderup.exception.InsufficientStockException;
import com.example.orderup.exception.OrderProcessingException;
import com.example.orderup.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleProductNotFound() {
        ProductNotFoundException ex = new ProductNotFoundException("Product not found");
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Product Not Found", response.getBody().getError());
        assertEquals("Product not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleInsufficientStock() {
        InsufficientStockException ex = new InsufficientStockException("Not enough stock");
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStock(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Insufficient Stock", response.getBody().getError());
        assertEquals("Not enough stock", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleOrderProcessing() {
        OrderProcessingException ex = new OrderProcessingException("Database error", new RuntimeException());
        ResponseEntity<ErrorResponse> response = handler.handleOrderProcessing(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Order Processing Error", response.getBody().getError());
        assertEquals("Database error", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Something went wrong", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
