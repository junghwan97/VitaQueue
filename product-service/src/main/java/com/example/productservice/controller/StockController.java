package com.example.productservice.controller;

import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
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

    @PostMapping("/{productId}/increase-stock")
    public ApiResponse<String> increaseStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        stockService.restoreReservedStock(productId, quantity);
        return ApiResponse.success("재고가 감소되었습니다.");
    }

    @PostMapping("/reserve")
    public ApiResponse<Boolean> reserveStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        boolean success = stockService.reserveStock(productId, quantity);
        if (!success) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고가 부족합니다.");
        }
        return ApiResponse.success(true);
    }
}
