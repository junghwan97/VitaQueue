package com.example.productservice.controller;

import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class StockController {

    StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/checkCount")
    public Integer checkCount(@RequestParam Long productId) {
//        ProductStockEntity productStockEntity = stockService.getProductStockByProductId(productId);
//        ProductStockResponse productStockResponse = new ProductStockResponse();
//        return productStockResponse.builder().id(productStockEntity.getId()).productId(productStockEntity.getProductId()).stock(productStockEntity.getStock()).build();
        return stockService.getProductStockByProductId(productId);
    }

    @PostMapping("/saveStockCount")
    public void saveProductStock(@RequestBody StockRequest stockRequest) {
        stockService.saveProductStock(stockRequest);
    }

    @PostMapping("/{productId}/decrease-stock")
    public ApiResponse<String> decreaseStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        stockService.decreaseStock(productId, quantity);
        return ApiResponse.success("재고가 감소되었습니다.");
    }
}
