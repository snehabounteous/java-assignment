package com.example.orderup.service;

import com.example.orderup.dto.OrderRequest;
import com.example.orderup.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    int getProductStock(Long productId);

}
