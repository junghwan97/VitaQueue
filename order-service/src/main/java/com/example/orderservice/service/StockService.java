package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.dto.request.StockRequest;
import com.example.orderservice.dto.response.ProductStockResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StockService {

    private final ProductServiceClient productService;

    public StockService(ProductServiceClient productService) {
        this.productService = productService;
    }

    public ProductStockResponse decreaseStock(Long productId, int quantity) {
        ProductStockResponse productStock = productService.checkCount(productId);
        productStock.decreaseStock((long) quantity);
        productService.saveProductStock(new StockRequest(productId, productStock.getStock()));
        return productStock;
    }

    public BigDecimal fetchProductPrice(Long productId) {
        return productService.getProduct(productId).getResult().getPrice();
    }
}
