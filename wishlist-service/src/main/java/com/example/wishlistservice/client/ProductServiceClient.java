package com.example.wishlistservice.client;


import com.example.wishlistservice.dto.response.ApiResponse;
import com.example.wishlistservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable("productId") Long productId);

}
