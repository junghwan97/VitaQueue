package com.example.orderservice.controller;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.OrderProductResponse;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-service")
public class OrderController {
    Environment env;
    OrderService orderService;

    @Autowired
    public OrderController(Environment env, OrderService orderService) {
        this.env = env;
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Long> saveOrder(@RequestBody @Valid List<OrderRequest> orderSaveRequest, @RequestHeader("X-User-Id") String userId) {
        // 주문을 저장하고 생성된 주문 ID를 반환
        Long orderId = orderService.saveOrder(orderSaveRequest, Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body(orderId);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") Long userId) {
        // 주문 취소
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.status(HttpStatus.OK).body("주문이 취소되었습니다.");
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<OrderProductResponse>> getOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") Long userId) {
        // 주문 상세 조회
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrder(orderId, userId));
    }

    @PostMapping("/orders/return/{orderId}")
    public ResponseEntity<String> returnOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") Long userId) {
        orderService.returnOrder(orderId, userId);
        return ResponseEntity.status(HttpStatus.OK).body("주문이 취소되었습니다.");
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderProductResponse>> getOrders(@RequestParam Long userId ) throws Exception {
        List<OrderProductResponse> orderList = orderService.getOrdersByUserId(Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body(orderList);
    }
}
