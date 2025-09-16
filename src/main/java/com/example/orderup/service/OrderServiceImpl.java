package com.example.orderup.service;

import com.example.orderup.dto.OrderRequest;
import com.example.orderup.dto.OrderResponse;
import com.example.orderup.exception.InsufficientStockException;
import com.example.orderup.exception.OrderProcessingException;
import com.example.orderup.exception.ProductNotFoundException;
import com.example.orderup.entity.Order;
import com.example.orderup.entity.Product;
import com.example.orderup.repository.OrderRepository;
import com.example.orderup.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;



    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        try {
            Product product = productRepository.findByIdForUpdate(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

            if (product.getStock() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - request.getQuantity());
            productRepository.save(product);

            Order order = Order.builder()
                    .customerName(request.getCustomerName())
                    .quantity(request.getQuantity())
                    .product(product)
                    .build();

            Order savedOrder = orderRepository.save(order);

            return OrderResponse.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(savedOrder.getQuantity())
                    .message("Order placed successfully")
                    .build();

        } catch (ProductNotFoundException | InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to process order: " + e.getMessage(), e);
        }
    }
    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        return OrderResponse.builder()
                .orderId(order.getId())
                .productId(order.getProduct().getId())
                .productName(order.getProduct().getName())
                .quantity(order.getQuantity())
                .message("Order retrieved successfully")
                .build();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> OrderResponse.builder()
                        .orderId(order.getId())
                        .productId(order.getProduct().getId())
                        .productName(order.getProduct().getName())
                        .quantity(order.getQuantity())
                        .message("Order retrieved successfully")
                        .build())
                .toList();
    }

    @Override
    public int getProductStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        return product.getStock();
    }
}
