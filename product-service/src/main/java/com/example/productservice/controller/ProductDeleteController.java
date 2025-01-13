package com.example.productservice.controller;

import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.service.ProductDeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class ProductDeleteController {

    ProductDeleteService productDeleteService;

    @Autowired
    public ProductDeleteController(ProductDeleteService productDeleteService) {
        this.productDeleteService = productDeleteService;
    }

    @DeleteMapping("/products/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable Long productId,
                                             @RequestHeader("X-User-Role") String role) {
        productDeleteService.deleteProduct(productId, role);
        return ApiResponse.success("상품이 삭제되었습니다.");
    }
}
