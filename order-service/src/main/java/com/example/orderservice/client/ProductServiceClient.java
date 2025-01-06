package com.example.orderservice.client;

import com.example.orderservice.dto.request.StockRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.ProductResponse;
import com.example.orderservice.dto.response.ProductStockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    // 상품 재고 조회
    @GetMapping("/checkCount")
    ProductStockResponse checkCount(@RequestParam Long productId);

    @PostMapping("/saveStockCount")
    void saveProductStock(@RequestBody StockRequest stockRequest);

    @GetMapping("/products/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable Long productId);
}