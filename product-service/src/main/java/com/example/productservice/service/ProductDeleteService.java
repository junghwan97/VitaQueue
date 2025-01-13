package com.example.productservice.service;

import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductEntity;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductDeleteService {

    ProductRepository productRepository;
    ProductStockRepository productStockRepository;

    @Autowired
    public ProductDeleteService(ProductRepository productRepository,
                                ProductStockRepository productStockRepository) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
    }

    public void deleteProduct(Long productId, String role) {
        validateAdminRole(role);
        ProductEntity productEntity = getProductEntity(productId);
        productRepository.delete(productEntity);
    }

    // 관리자 권한 확인
    private static void validateAdminRole(String role) {
        if (!"ROLE_ADMIN".equals(role)) throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "상품에 대한 권한이 없습니다.");
    }

    //상품 확인
    public ProductEntity getProductEntity(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId).orElse(null);
        if (productEntity == null)
            throw new VitaQueueException(ErrorCode.PRODUCT_NOT_FOUND, String.format("%d번 상품은 존재하지 않습니다.", productId));
        return productEntity;
    }
}
