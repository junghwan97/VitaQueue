package com.example.orderservice.controller;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.service.OrderCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class OrderCreateController {

    private final OrderCreateService orderCreateService;

    @PostMapping("/orders")
    public ApiResponse<Long> saveOrder(@RequestBody @Valid List<OrderRequest> orderSaveRequest, @RequestHeader("X-User-Id") String userId) {
        // 주문을 저장하고 생성된 주문 ID를 반환
        Long orderId = orderCreateService.saveOrder(orderSaveRequest, Long.valueOf(userId));
        return ApiResponse.success(orderId);
    }
}
