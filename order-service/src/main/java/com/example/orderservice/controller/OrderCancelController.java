package com.example.orderservice.controller;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.service.OrderCancelService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class OrderCancelController {

    private final OrderCancelService orderCancelService;

    public OrderCancelController(OrderCancelService orderCancelService) {
        this.orderCancelService = orderCancelService;
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<String> cancelOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") String userId) {
        // 주문 취소
        orderCancelService.cancelOrder(orderId, Long.valueOf(userId));
        return ApiResponse.success("주문이 취소되었습니다.");
    }
}
