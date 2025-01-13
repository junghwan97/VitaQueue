package com.example.orderservice.controller;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.service.OrderReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class OrderReturnController {

    private final OrderReturnService orderReturnService;

    @Autowired
    public OrderReturnController(OrderReturnService orderReturnService) {
        this.orderReturnService = orderReturnService;
    }

    @PostMapping("/orders/return/{orderId}")
    public ApiResponse<String> returnOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") String userId) {
        orderReturnService.returnOrder(orderId, Long.valueOf(userId));
        return ApiResponse.success("주문이 취소되었습니다.");
    }

}
