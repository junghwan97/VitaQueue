package com.example.productservice.service;

import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.jpa.ProductEntity;
import com.example.productservice.jpa.ProductStockEntity;
import lombok.SneakyThrows;

import java.util.List;

public interface ProductService {
    @SneakyThrows
    List<ProductResponse> getProducts(Long cursorId, Integer size);

    ProductResponse getProduct(Long productId) throws Exception;

    void setProduct(ProductRequest request, Long userId, String role);

    void updateProduct(Long productId, ProductUpdateRequest request, Long userId, String role);

    void deleteProduct(Long productId, String role);

    ProductStockEntity getProductStockByProductId(Long productId);

    void saveProductStock(StockRequest stockRequest);

    ProductEntity getProductEntity(Long productId);
}
