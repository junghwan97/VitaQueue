package com.example.productservice.service;


import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductEntity;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductStockRepository productStockRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductStockRepository productStockRepository) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
    }

    @Override
    public List<ProductResponse> getProducts(Long cursorId, Integer size) {
        List<ProductEntity> productEntityList;
        // 커서가 0이라면 최신 등록 제품를 size만큼 반환
        if (cursorId == 0) productEntityList = productRepository.findAllOrderByIdDesc(size);
            // 커서가 0이 아니라면 cursor번 등록 제품부터 size만큼 반환
        else productEntityList = productRepository.findAllOrderByIdDescWithCursor(cursorId, size);

        List<ProductResponse> productResponses = productEntityList.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        return productResponses;
    }

    @Override
    public ProductResponse getProduct(Long productId) {
        // 상품 아이디로 상품 조회 / 존재하지 않으면 예외처리
        ProductEntity productEntity = getProductEntity(productId);
        ProductStockEntity stock = getProductStockByProductId(productId);
        return ProductResponse.fromEntity(productEntity, stock.getStock());
    }

    @Override
    public void setProduct(ProductRequest request, Long userId, String role) {
        // 유저 권한 확인
        validateAdminRole(role);
        // 관리자 권한이면 상품 저장
        ProductEntity productEntity = productRepository.save(new ProductEntity().of(request, userId));
        // 재고 정보 저장
        productStockRepository.save(new ProductStockEntity().of(productEntity.getId(), request.getStock()));
    }

    @Override
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

    @Override
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

    // 상품 재고 등록 확인
    @Transactional
    public ProductStockEntity getProductStockByProductId(Long productId) {
        ProductStockEntity productStockEntity = getProductStockEntity(productId);
        if (productStockEntity == null)
            throw new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND);
        return productStockEntity;
    }


    // 재고 저장
    @Transactional
    public void saveProductStock(StockRequest stockRequest) {
        ProductStockEntity productStockEntity = getProductStockEntity(stockRequest.getProductId());
        if (productStockEntity == null)
            throw new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND);

        productStockEntity.setProductId(stockRequest.getProductId());
        productStockEntity.setStock(stockRequest.getStock());
    }

    // 상품 재고 정보 조회
    @Transactional
    public ProductStockEntity getProductStockEntity(Long productId) {
//        return productStockRepository.findByProductId(productId).orElse(null);
        return productStockRepository.findByProductIdWithLock(productId);
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);

        if (quantity < 0) {
            throw new VitaQueueException(ErrorCode.STOCK_DECREASE_NEGATIVE, "재고 감소 수량은 음수일 수 없습니다.");
        }
        if (stockEntity.getStock() < quantity) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고가 부족합니다.");
        }

        stockEntity.setStock(stockEntity.getStock() - quantity);
        productStockRepository.save(stockEntity);
    }

}
