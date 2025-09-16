package com.example.orderup.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OrderLoggingAspect {

    @Around("execution(* com.example.orderup.service.OrderService.placeOrder(..))")
    public Object logOrderProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        log.info("Order processing started. Request: {}", args.length > 0 ? args[0] : "No args");

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Order processing finished successfully in {} ms. Response: {}", duration, result);
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;
            log.error("Order processing failed after {} ms. Error: {}", duration, t.getMessage());
            throw t;
        }
    }
}
