package com.dsaurabh.orderservice.controller;

import com.dsaurabh.orderservice.dto.OrderRequest;
import com.dsaurabh.orderservice.event.OrderEvent;
import com.dsaurabh.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name="inventory", fallbackMethod = "fallBackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest){
        CompletableFuture<String> result = CompletableFuture.supplyAsync(()-> orderService.placeOrder(orderRequest));

        kafkaTemplate.send("notification_topic" , new OrderEvent("Order Placed Successfully!!!"));
        return result;
    }

    public CompletableFuture<String> fallBackMethod(OrderRequest orderRequest, RuntimeException exception){
        return CompletableFuture.supplyAsync(()->"Oops Something went wrong, Please try later!!");
    }
}
