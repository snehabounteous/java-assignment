package com.example.orderup.aspect;

import com.example.orderup.dto.OrderRequest;
import com.example.orderup.dto.OrderResponse;
import com.example.orderup.service.OrderService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderLoggingAspectTest {

    @MockitoBean
    private OrderService orderService;

    @Test
    void testAspectAroundOrderService() {

        OrderRequest request = new OrderRequest(1L, "AspectTest User", 1);
        OrderResponse mockResponse = OrderResponse.builder()
                .orderId(10L)
                .productId(1L)
                .productName("AspectTest Product")
                .quantity(1)
                .message("Order placed successfully")
                .build();

        Mockito.when(orderService.placeOrder(Mockito.any(OrderRequest.class))).thenReturn(mockResponse);

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals("Order placed successfully", response.getMessage());
    }
}
