package com.example.productservice.service;

import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    ProductRepository productRepository;
    ProductStockRepository productStockRepository;
    RedisService redisService;

    public StockService(ProductRepository productRepository, ProductStockRepository productStockRepository, RedisService redisService) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
        this.redisService = redisService;
    }

    // 상품 재고 확인
    public Integer getProductStockByProductId(Long productId) {
        Integer stock = redisService.getValue("productStock:" + productId);
        if (stock != null) {
            return stock;
        } else {
            ProductStockEntity stockEntity = productStockRepository.findByProductId(productId)
                    .orElseThrow(() -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND));
            redisService.setValues("productStock:" + productId, stockEntity.getStock());
            return stockEntity.getStock();
        }
    }

    // 재고 저장
    @Transactional
    public void saveProductStock(StockRequest stockRequest) {
        ProductStockEntity productStockEntity = productStockRepository.findByProductId(stockRequest.getProductId()).orElse(null);
        if (productStockEntity == null) throw new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND);
        productStockEntity.setProductId(stockRequest.getProductId());
        productStockEntity.setStock(stockRequest.getStock());
    }

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        if (quantity < 0) {
            throw new VitaQueueException(ErrorCode.STOCK_DECREASE_NEGATIVE, "재고 감소 수량은 음수일 수 없습니다.");
        }

        String redisKey = "productStock:" + productId;
        Integer stock = redisService.getValue(redisKey);

        if (stock == null) {
            // Redis에 없으면 DB에서 조회
            ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
            stock = stockEntity.getStock();
            redisService.setValues(redisKey, stock);
        }

        if (stock < quantity) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고가 부족합니다.");
        }

        // Redis에서 재고 감소
        redisService.decrement(redisKey, quantity);
//        stockEntity.setStock(stockEntity.getStock() - quantity);
//        productStockRepository.save(stockEntity);
    }

    // 재고 증가 (추가 구현)
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        if (quantity < 0) {
            throw new VitaQueueException(ErrorCode.STOCK_INCREASE_NEGATIVE, "재고 증가 수량은 음수일 수 없습니다.");
        }

        String redisKey = "productStock:" + productId;
        Integer stock = redisService.getValue(redisKey);

        if (stock == null) {
            // Redis에 없으면 DB에서 조회
            ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
            stock = stockEntity.getStock();
            redisService.setValues(redisKey, stock);
        }

        // Redis에서 재고 증가
        redisService.increment(redisKey, quantity);
    }

    // Redis 데이터를 주기적으로 DB에 반영
    @Scheduled(cron = "0 0 * * * ?") // 매 정각마다 실행
    @Transactional
    public void syncRedisToDatabase() {
        List<String> keys = redisService.getKeys("productStock:*");

        for (String key : keys) {
            Long productId = Long.valueOf(key.split(":")[1]);
            Integer stock = redisService.getValue(key);

            ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
            stockEntity.setStock(stock);
        }

        productStockRepository.flush(); // 변경 사항 DB에 반영
    }
}
