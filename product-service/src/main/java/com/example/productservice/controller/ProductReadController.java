package com.example.productservice.controller;

import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.service.ProductReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class ProductReadController {

    ProductReadService productReadService;

    @Autowired
    public ProductReadController(ProductReadService productReadService) {
        this.productReadService = productReadService;
    }

    // 등록되어 있는 상품의 리스트 조회
    @GetMapping("/products")
    public ApiResponse<List<ProductResponse>> getProducts(@RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
                                                          @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                          @RequestParam(value = "event", defaultValue = "false") Boolean event){
        return ApiResponse.success(productReadService.getProducts(cursorId, size, event));
    }

    // 특정 상품 조회
    @GetMapping("/products/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long productId) throws Exception {
        return ApiResponse.success(productReadService.getProduct(productId));
    }
}
