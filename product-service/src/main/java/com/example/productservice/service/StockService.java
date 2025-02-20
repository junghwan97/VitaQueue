package com.example.productservice.service;

import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class StockService {

    ProductRepository productRepository;
    ProductStockRepository productStockRepository;
    RedissonClient redissonClient;
    RedisService redisService;

    private static final String STOCK_LOCK_PREFIX = "stock:lock:";
    private static final String STOCK_DATA_PREFIX = "product-Stock:";

    public StockService(ProductRepository productRepository,
                        ProductStockRepository productStockRepository,
                        RedisService redisService,
                        RedissonClient redissonClient) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
        this.redisService = redisService;
        this.redissonClient = redissonClient;
    }

    // 상품 재고 확인
    public Integer getProductStockByProductId(Long productId) {
        Integer stock = redisService.getValue("product-Stock:" + productId);
        if (stock != null) {
            return stock;
        } else {
            ProductStockEntity stockEntity = productStockRepository.findByProductId(productId)
                    .orElseThrow(() -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND));
            redisService.setValues("product-Stock:" + productId, String.valueOf(stockEntity.getStock()));
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

    public void decreaseStock(Long productId, Integer quantity) {
        String stockKey = STOCK_DATA_PREFIX + productId;

        String luaScript =
                "if redis.call('EXISTS', KEYS[1]) == 1 and tonumber(redis.call('GET', KEYS[1])) >= tonumber(ARGV[1]) then " +
                        "   return redis.call('DECRBY', KEYS[1], ARGV[1]) " +
                        "else " +
                        "   return -1 " +
                        "end";

        Long updatedStock = redisService.executeLuaScript(luaScript,
                Collections.singletonList(stockKey), Collections.singletonList(quantity.toString()));

        if (updatedStock == -1) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고 부족");
        }
    }

    // 재고 증가
    public void increaseStock(Long productId, Integer quantity) {
        if (quantity < 0) {
            throw new VitaQueueException(ErrorCode.STOCK_INCREASE_NEGATIVE, "재고 증가 수량은 음수일 수 없습니다.");
        }

        String stockKey = STOCK_DATA_PREFIX + productId;

        // Lua 스크립트
        String luaScript =
                "if redis.call('EXISTS', KEYS[1]) == 1 then " +  // 키가 존재하는지 확인
                        "   return redis.call('INCRBY', KEYS[1], ARGV[1]) " + // 재고 증가
                        "else " +
                        "   return -1 " +  // 재고 정보가 없을 경우 예외 처리
                        "end";

        Long updatedStock = redisService.executeLuaScript(luaScript,
                Collections.singletonList(stockKey), Collections.singletonList(quantity.toString()));

        if (updatedStock == -1) {
            throw new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND, "현재 재고 정보가 없습니다.");
        }
    }



    // Redis 데이터를 주기적으로 DB에 반영
    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void syncRedisToDatabase() {
        List<String> keys = redisService.getKeys("product-Stock:*");

        for (String key : keys) {
            Long productId = Long.valueOf(key.split(":")[1]);
            Integer stock = redisService.getValue(key);

            ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
            stockEntity.setStock(stock);
        }

        productStockRepository.flush();
    }

}
