package com.example.productservice.service;

import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

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

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        String lockKey = STOCK_LOCK_PREFIX + productId;
        String stockKey = STOCK_DATA_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 20, TimeUnit.SECONDS)) {
                System.out.println("🔒 락 획득 성공: " + productId);

                Integer currentStockCount = redisService.getValue(stockKey);
                System.out.println("📌 현재 Redis 재고: " + currentStockCount);

                if (currentStockCount == null) {
                    System.out.println("⚠️ Redis 값이 없음! DB에서 조회 후 재설정.");
                    ProductStockEntity stockEntity = productStockRepository.findByProductId(productId)
                            .orElseThrow(() -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND));
                    redisService.setValues(stockKey, String.valueOf(stockEntity.getStock()));
                    currentStockCount = stockEntity.getStock();
                }

                System.out.println("📌 현재 Redis 재고: " + currentStockCount);

                if (currentStockCount < quantity) {
                    System.out.println("🚨 재고 부족 예외 발생!");
                    throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "예약된 재고보다 많은 수량을 결제할 수 없습니다.");
                }

                redisService.decrement(stockKey, quantity);
                System.out.println("✅ Redis 재고 감소 후: " + redisService.getValue(stockKey));

                ProductStockEntity stockEntity = productStockRepository.findByProductId(productId).orElseThrow(()->new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH));
                System.out.println("📌 DB 재고 감소 전: " + stockEntity.getStock());
                stockEntity.setStock(stockEntity.getStock() - quantity);
                productStockRepository.save(stockEntity);
                productStockRepository.flush(); // 변경 사항 즉시 반영
                System.out.println("✅ DB 재고 감소 후: " + stockEntity.getStock());

            } else {
                System.out.println("❌ 락 획득 실패: " + productId);
                throw new VitaQueueException(ErrorCode.LOCK_ACQUISITION_FAILED,
                        "재고 차감 중 락 획득 실패");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("재고 차감 중 오류 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    // 재고 증가
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        String lockKey = "productStock" + productId;
        String stockKey = "productStock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득: 10초 대기, 5초 락 유지
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                if (quantity < 0) {
                    throw new VitaQueueException(ErrorCode.STOCK_INCREASE_NEGATIVE, "재고 증가 수량은 음수일 수 없습니다.");
                }

                // Redis에서 현재 재고 확인
                Integer currentStockCount = redisService.getValue(stockKey);
                if (currentStockCount == null) {
                    throw new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND, "현재 재고 정보가 없습니다.");
                }

                // DB에서 재고 업데이트
                ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
                stockEntity.setStock(currentStockCount + quantity);
                productStockRepository.save(stockEntity);

                // Redis의 재고 업데이트
                redisService.increment(stockKey, quantity);

            } else {
                throw new VitaQueueException(ErrorCode.LOCK_ACQUISITION_FAILED, "재고 복원 중 락 획득 실패");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("재고 복원 중 오류 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    // Redis 데이터를 주기적으로 DB에 반영
//    @Scheduled(cron = "0 */5 * * * ?")
//    @Transactional
//    public void syncRedisToDatabase() {
//        List<String> keys = redisService.getKeys("product-Stock:*");
//
//        for (String key : keys) {
//            Long productId = Long.valueOf(key.split(":")[1]);
//            Integer stock = redisService.getValue(key);
//
//            ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
//            stockEntity.setStock(stock);
//        }
//
//        productStockRepository.flush();
//    }

}
