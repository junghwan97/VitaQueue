package com.example.productservice.controller;

import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.service.ProductWriteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class ProductWriteController {

    ProductWriteService productWriteService;

    @Autowired
    public ProductWriteController(ProductWriteService productWriteService) {
        this.productWriteService = productWriteService;
    }

    // 상품 등록
    @PostMapping("/products/post")
    public ApiResponse<String> setProduct(@Valid @RequestBody ProductRequest request,
                                          @RequestHeader("X-User-Id") String userId,
                                          @RequestHeader("X-User-Role") String role) {
        // 관리자 권한만 등록 가능
        productWriteService.setProduct(request, Long.valueOf(userId), role);
        return ApiResponse.success("상품이 등록되었습니다.");
    }

    // 특정 상품 수정
    @PatchMapping("/products/{productId}")
    public ApiResponse<String> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductUpdateRequest request,
                                             @RequestHeader("X-User-Id") String userId,
                                             @RequestHeader("X-User-Role") String role) {
        productWriteService.updateProduct(productId, request, Long.valueOf(userId), role);
        return ApiResponse.success("상품이 수정되었습니다.");
    }

}
