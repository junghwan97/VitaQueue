package com.example.orderservice.controller;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderProductResponse;
import com.example.orderservice.service.OrderReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class OrderReadController {

    private final OrderReadService orderReadService;

    @GetMapping("/orders")
    public ApiResponse<List<OrderProductResponse>> getOrders(@RequestHeader("X-User-Id") String userId) {
        List<OrderProductResponse> orderList = orderReadService.getOrdersByUserId(Long.valueOf(userId));
        return ApiResponse.success(orderList);
    }

    // 주문 상세 조회
    @GetMapping("/orders/{orderId}")
    public ApiResponse<List<OrderProductResponse>> getOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(orderReadService.getOrder(orderId, Long.valueOf(userId)));
    }

    @GetMapping("/order")
    public ApiResponse<List<OrderProductResponse>> getOrderByUser(@RequestParam String userId) {
        List<OrderProductResponse> orderList = orderReadService.getOrdersByUserId(Long.valueOf(userId));
        return ApiResponse.success(orderList);
    }
}
