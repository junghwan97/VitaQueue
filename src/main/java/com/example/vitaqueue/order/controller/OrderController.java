package com.example.vitaqueue.order.controller;

import com.example.vitaqueue.common.ApiResponse;
import com.example.vitaqueue.order.dto.request.OrderRequest;
import com.example.vitaqueue.order.service.OrderService;
import com.example.vitaqueue.orderProduct.dto.response.OrderProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<Long> saveOrder(@RequestBody @Valid List<OrderRequest> orderSaveRequest, Authentication authentication) throws Exception {
        // 주문을 저장하고 생성된 주문 ID를 반환
        Long orderId = orderService.saveOrder(orderSaveRequest, authentication);
        return ApiResponse.success(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        // 주문 취소
        orderService.cancelOrder(orderId, authentication);
        return ApiResponse.success();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<List<OrderProductResponse>> getOrder(@PathVariable Long orderId, Authentication authentication) {
        // 주문 상세 조회
        return ApiResponse.success(orderService.getOrder(orderId, authentication));
    }

    @PostMapping("/return/{orderId}")
    public ApiResponse<Void> returnOrder(@PathVariable Long orderId, Authentication authentication) {
        orderService.returnOrder(orderId, authentication);
        return ApiResponse.success();
    }
}

