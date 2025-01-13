package com.example.productservice.service;

import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductEntity;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductWriteService {

    ProductRepository productRepository;
    ProductStockRepository productStockRepository;

    @Autowired
    public ProductWriteService(ProductRepository productRepository,
                              ProductStockRepository productStockRepository
    ) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
    }

    public void setProduct(ProductRequest request, Long userId, String role) {
        // 유저 권한 확인
        validateAdminRole(role);
        // 관리자 권한이면 상품 저장
        ProductEntity productEntity = productRepository.save(new ProductEntity().of(request, userId));
        // 재고 정보 저장
        productStockRepository.save(new ProductStockEntity().of(productEntity.getId(), request.getStock()));
    }

    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest request, Long userId, String role) {
        // 권한 확인
        validateAdminRole(role);
        ProductEntity productEntity = getProductEntity(productId);
        if (!productEntity.getUserId().equals(userId)) throw new VitaQueueException(ErrorCode.INVALID_PERMISSION);
        if (request.getName() != null) productEntity.setName(request.getName());
        if (request.getPrice() != null) productEntity.setPrice(request.getPrice());
        if (request.getDescript() != null) productEntity.setDescript(request.getDescript());
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
