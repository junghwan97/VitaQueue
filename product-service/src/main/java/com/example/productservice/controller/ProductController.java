package com.example.productservice.controller;


import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.dto.response.ProductStockResponse;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
                                                             @RequestParam(value = "size", defaultValue = "10") Integer size) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProducts(cursorId, size));
    }

    // 특정 상품 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProduct(productId));
    }

    // 상품 등록
    @PostMapping("/post")
    public ResponseEntity<String> setProduct(@Valid @RequestBody ProductRequest request,
                                             @RequestHeader("X-User-Id") String userId,
                                             @RequestHeader("X-User-Role") String role) {
        // 관리자 권한만 등록 가능
        productService.setProduct(request, Long.valueOf(userId), role);
        return ResponseEntity.status(HttpStatus.CREATED).body("상품이 등록되었습니다.");
    }

    // 특정 상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<String> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductUpdateRequest request,
                                                @RequestHeader("X-User-Id") String userId,
                                                @RequestHeader("X-User-Role") String role) {
        productService.updateProduct(productId, request, Long.valueOf(userId), role);
        return ResponseEntity.status(HttpStatus.OK).body("상품이 수정되었습니다.");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId,
                                                @RequestHeader("X-User-Role") String role) {
        productService.deleteProduct(productId, role);
        return ResponseEntity.status(HttpStatus.OK).body("상품이 삭제되었습니다.");
    }
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

//    @GetMapping("/product")
//    public ProductResponse getProduct(@RequestParam Long productId) {
//        ProductEntity productEntity = productService.getProductEntity(productId);
//        return ProductResponse.builder()
//                .id(productEntity.getId())
//                .name(productEntity.getName())
//                .sellerId(productEntity.getUserId())
//                .price(productEntity.getPrice())
//                .descript(productEntity.getDescript())
//                .build();
//    }
}

