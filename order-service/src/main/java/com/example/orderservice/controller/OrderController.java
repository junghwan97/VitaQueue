package com.example.orderservice.controller;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderProductResponse;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Autowired
    public OrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    public ApiResponse<Long> saveOrder(@RequestBody @Valid List<OrderRequest> orderSaveRequest, @RequestHeader("X-User-Id") String userId) {
        // 주문을 저장하고 생성된 주문 ID를 반환
        Long orderId = orderService.saveOrder(orderSaveRequest, Long.valueOf(userId));
        return ApiResponse.success(orderId);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<String> cancelOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") String userId) {
        // 주문 취소
        orderService.cancelOrder(orderId, Long.valueOf(userId));
        return ApiResponse.success("주문이 취소되었습니다.");
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<List<OrderProductResponse>> getOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") String userId) {
        // 주문 상세 조회
        return ApiResponse.success(orderService.getOrder(orderId, Long.valueOf(userId)));
    }

    @PostMapping("/orders/return/{orderId}")
    public ApiResponse<String> returnOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") String userId) {
        orderService.returnOrder(orderId, Long.valueOf(userId));
        return ApiResponse.success("주문이 취소되었습니다.");
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderProductResponse>> getOrders(@RequestHeader("X-User-Id") String userId) {
        List<OrderProductResponse> orderList = orderService.getOrdersByUserId(Long.valueOf(userId));
        return ApiResponse.success(orderList);
    }

    @GetMapping("/orderByuser")
    public ApiResponse<List<OrderProductResponse>> getOrderByUser(@RequestParam Long userId) {
        List<OrderProductResponse> orderList = orderService.getOrdersByUserId(userId);
        return ApiResponse.success(orderList);
    }

    @PostMapping("/orders/{orderId}/enter")
    public ApiResponse<String> enterPayment(@PathVariable Long orderId) {
        orderService.enterPayment(orderId);
        return ApiResponse.success("결제에 진입합니다.");
    }

    @PostMapping("/orders/{orderId}/pay")
    public ApiResponse<String> processPayment(@PathVariable Long orderId) {
        String isSuccess = paymentService.processPayment(orderId);
        return ApiResponse.success("결제가 완료되었습니다.");
    }
}
