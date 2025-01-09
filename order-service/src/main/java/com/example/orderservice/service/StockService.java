package com.example.orderservice.service;

import org.springframework.stereotype.Service;

@Service
public class StockService {

//    private final ProductServiceClient productService;
//
//    public StockService(ProductServiceClient productService) {
//        this.productService = productService;
//    }
//
//    @Transactional
//    public ProductStockResponse decreaseStock(Long productId, int quantity) {
//        ProductStockResponse productStock = productService.checkCount(productId);
//        productStock.decreaseStock((long) quantity);
//        productService.decreaseStock(productId, quantity);
//        productService.saveProductStock(new StockRequest(productId, productStock.getStock()));
//        return productStock;
//    }
//
//    @Transactional
//    public ProductResponse getProduct(Long productId) {
//        return productService.getProduct(productId).getResult();
//    }
}
