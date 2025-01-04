package com.example.productservice.controller;


import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.dto.response.ProductStockResponse;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class ProductController {
    Environment env;
    ProductService productService;

    @Autowired
    public ProductController(Environment env, ProductService productService) {
        this.env = env;
        this.productService = productService;
    }

    // 등록되어 있는 상품의 리스트 조회
    @GetMapping("/products")
    public ApiResponse<List<ProductResponse>> getProducts(@RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
                                                          @RequestParam(value = "size", defaultValue = "10") Integer size) throws Exception {
        return ApiResponse.success(productService.getProducts(cursorId, size));
    }

    // 특정 상품 조회
    @GetMapping("/products/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long productId) throws Exception {
        return ApiResponse.success(productService.getProduct(productId));
    }

    // 상품 등록
    @PostMapping("/products/post")
    public ApiResponse<String> setProduct(@Valid @RequestBody ProductRequest request,
                                             @RequestHeader("X-User-Id") String userId,
                                             @RequestHeader("X-User-Role") String role) {
        // 관리자 권한만 등록 가능
        productService.setProduct(request, Long.valueOf(userId), role);
        return ApiResponse.success("상품이 등록되었습니다.");
    }

    // 특정 상품 수정
    @PatchMapping("/products/{productId}")
    public ApiResponse<String> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductUpdateRequest request,
                                                @RequestHeader("X-User-Id") String userId,
                                                @RequestHeader("X-User-Role") String role) {
        productService.updateProduct(productId, request, Long.valueOf(userId), role);
        return ApiResponse.success("상품이 수정되었습니다.");
    }

    @DeleteMapping("/products/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable Long productId,
                                                @RequestHeader("X-User-Role") String role) {
        productService.deleteProduct(productId, role);
        return ApiResponse.success("상품이 삭제되었습니다.");
    }

    // 외부 feign clint
    // 재고 조회
    @GetMapping("/checkCount")
    public ProductStockResponse checkCount(@RequestParam Long productId) {
        ProductStockEntity productStockEntity = productService.getProductStockByProductId(productId);
        ProductStockResponse productStockResponse = new ProductStockResponse();
        return productStockResponse.builder().id(productStockEntity.getId()).productId(productStockEntity.getProductId()).stock(productStockEntity.getStock()).build();
    }

    @PostMapping("/saveStockCount")
    public void saveProductStock(@RequestBody StockRequest stockRequest) {
        productService.saveProductStock(stockRequest);
    }
}

