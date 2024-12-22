package com.example.vitaqueue.product.controller;

import com.example.vitaqueue.common.ApiResponse;
import com.example.vitaqueue.product.dto.request.ProductRequest;
import com.example.vitaqueue.product.dto.response.ProductResponse;
import com.example.vitaqueue.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // 등록되어 있는 상품의 리스트 조회
    @GetMapping
    public ApiResponse<List<ProductResponse>> getProducts(@RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
                                                          @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ApiResponse.success(productService.getProducts(cursorId, size));
    }

    // 특정 상품 조회
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long productId) {
        return ApiResponse.success(productService.getProduct(productId));
    }

    // 상품 등록
    @PostMapping
    public ApiResponse<Void> setProduct(@Valid @RequestBody ProductRequest request, Authentication authentication) {
        // 관리자 권한만 등록 가능
        productService.setProduct(request, authentication.getAuthorities());
        return ApiResponse.success();
    }
}
