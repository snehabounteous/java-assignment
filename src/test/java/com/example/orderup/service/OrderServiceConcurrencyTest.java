package com.example.orderup.service;

import com.example.orderup.dto.OrderRequest;
import com.example.orderup.entity.Product;
import com.example.orderup.repository.OrderRepository;
import com.example.orderup.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setStock(10);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testConcurrentOrderPlacement() throws InterruptedException, ExecutionException {
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(() -> {
                OrderRequest request = new OrderRequest();
                request.setProductId(testProduct.getId());
                request.setCustomerName(Thread.currentThread().getName());
                request.setQuantity(1); // Each thread tries to order 1 unit

                try {
                    orderService.placeOrder(request);
                    return "SUCCESS";
                } catch (Exception e) {
                    return "FAILED";
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        int successCount = 0;
        int failureCount = 0;

        for (Future<String> future : futures) {
            String result = future.get();
            if (result.equals("SUCCESS")) successCount++;
            else failureCount++;
        }

        assertEquals(10, successCount, "Exactly 10 orders should succeed");
        assertEquals(10, failureCount, "Remaining 10 orders should fail due to stock limit");

        int remainingStock = productRepository.findById(testProduct.getId()).get().getStock();
        assertEquals(0, remainingStock, "Stock should be 0 after successful orders");

        System.out.println("Success: " + successCount);
        System.out.println("Failed: " + failureCount);
    }
}
