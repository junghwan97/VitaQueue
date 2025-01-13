package com.example.userservice.client;

import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.OrderProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name="order-service")
public interface OrderServiceClient {

    @GetMapping("/order")
    ApiResponse<List<OrderProductResponse>> getOrderByUser(@RequestParam Long userId);

}
