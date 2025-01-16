package com.example.productservice.service;

import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductEntity;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductReadService {

    ProductRepository productRepository;
    ProductStockRepository productStockRepository;
    RedisService redisService;

    @Autowired
    public ProductReadService(ProductRepository productRepository,
                              ProductStockRepository productStockRepository,
                              RedisService redisService) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
        this.redisService = redisService;
    }

    public List<ProductResponse> getProducts(Long cursorId, Integer size, Boolean event) {
        List<ProductEntity> productEntityList;

        if (!event) {
            // 커서가 0이라면 최신 등록 제품를 size만큼 반환
            if (cursorId == 0) productEntityList = productRepository.findAllByIsFlashSaleFalseOrderByIdDesc(size);
                // 커서가 0이 아니라면 cursor번 등록 제품부터 size만큼 반환
            else productEntityList = productRepository.findAllByIsFlashSaleFalseOrderByIdDescWithCursor(cursorId, size);
        } else {
            // 커서가 0이라면 최신 선착순 등록 제품을 size만큼 반환
            if (cursorId == 0) productEntityList = productRepository.findAllByIsFlashSaleTrueOrderByIdDesc(size);
                // 커서가 0이 아니라면 cursor번 등록 선착순 제품부터 size만큼 반환
            else productEntityList = productRepository.findAllByIsFlashSaleTrueOrderByIdDescWithCursor(cursorId, size);
        }
        List<ProductResponse> productResponses = productEntityList.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        return productResponses;
    }

    public ProductResponse getProduct(Long productId) {
        // 상품 아이디로 상품 조회 / 존재하지 않으면 예외처리
        ProductEntity productEntity = getProductEntity(productId);
//        ProductStockEntity stockEntity = getProductStockByProductId(productId);
        Integer stock = redisService.getValue("productStock:" + productId);
        if (stock == null) {
            ProductStockEntity stockEntity = productStockRepository.findByProductId(productId)
                    .orElseThrow(() -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND));
            redisService.setValues("productStock:" + productId, stockEntity.getStock());
            stock = stockEntity.getStock();
        }
        return ProductResponse.fromEntity(productEntity, stock);
    }

    //상품 확인
    public ProductEntity getProductEntity(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId).orElse(null);
        if (productEntity == null)
            throw new VitaQueueException(ErrorCode.PRODUCT_NOT_FOUND, String.format("%d번 상품은 존재하지 않습니다.", productId));
        return productEntity;
    }

    // 상품 재고 확인
    public ProductStockEntity getProductStockByProductId(Long productId) {
        ProductStockEntity productStockEntity = productStockRepository.findByProductId(productId).orElse(null);
        if (productStockEntity == null)
            throw new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND);
        return productStockEntity;
    }

    @Scheduled(cron = "0 0 * * * ?") // 매 정각마다 실행
    @Transactional
    public void updateFlashSaleProducts() {
        Timestamp currentTime = Timestamp.from(Instant.now());
        List<ProductEntity> productsToUpdate = productRepository.findFlashSaleProducts(currentTime);
        for (ProductEntity product : productsToUpdate) {
            product.updateFlashSaleOpen(true);
        }
        productRepository.saveAll(productsToUpdate);
    }
}
