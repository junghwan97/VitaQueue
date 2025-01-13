package com.example.orderservice.controller;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.service.PaymentService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class OrderPayController {

    private final PaymentService paymentService;

    public OrderPayController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}/enter")
    public ApiResponse<String> enterPayment(@PathVariable Long orderId) {
        paymentService.enterPayment(orderId);
        return ApiResponse.success("결제 진입 성공");
    }

    @PostMapping("/orders/{orderId}/pay")
    public ApiResponse<String> processPayment(@PathVariable Long orderId) {
        String result = paymentService.processPayment(orderId);
        return ApiResponse.success(result);
    }
}
