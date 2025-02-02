package com.example.orderservice.client;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/checkCount")
    Long checkCount(@RequestParam Long productId);

//    @PostMapping("/reserve")
//    ApiResponse<Boolean> reserveStock(@RequestParam Long productId, @RequestParam Integer quantity);

    @GetMapping("/products/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable Long productId);

    @PostMapping("/{productId}/decrease-stock")
    ApiResponse<String> decreaseStock(@PathVariable Long productId, @RequestParam Integer quantity);

    @PostMapping("/{productId}/increase-stock")
    ApiResponse<String> increaseStock(@PathVariable Long productId, @RequestParam Integer quantity);

    @PostMapping("/{productId}/restore-reserved")
    ApiResponse<String> restoreReservedStock(@PathVariable Long productId, @RequestParam Integer quantity);
}