package com.example.productservice.service;

import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StockService {

    ProductRepository productRepository;
    ProductStockRepository productStockRepository;
    RedissonClient redissonClient;
    RedisService redisService;

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
        String lockKey = "product-lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 락 획득: 10초 대기, 5초 락 유지
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
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
                // DB 업데이트 추가됨
                ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
                stockEntity.setStock(stock - quantity);
                productStockRepository.save(stockEntity);
            } else {
                throw new VitaQueueException(ErrorCode.LOCK_ACQUISITION_FAILED, "재고 감소 중 락 획득 실패");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("재고 감소 중 오류 발생", e);
        } finally {
            lock.unlock(); // 락 해제
        }
    }

    // 재고 증가
    @Transactional
    public void restoreReservedStock(Long productId, Integer quantity) {
        String lockKey = "product-lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 락 획득: 10초 대기, 5초 락 유지
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                if (quantity < 0) {
                    throw new VitaQueueException(ErrorCode.STOCK_INCREASE_NEGATIVE, "재고 증가 수량은 음수일 수 없습니다.");
                }

                // 예약된 재고 키
                String reservedKey = "reservedStock:" + productId;

                // Redis에서 예약된 재고 확인 및 복원
                Integer reservedStock = redisService.getValue(reservedKey);
                if (reservedStock == null || reservedStock < quantity) {
                    throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "복원할 예약 재고가 부족합니다.");
                }

                // 예약된 재고 복원
                redisService.increment(reservedKey, quantity);

//                // DB 업데이트
//                ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
//                stockEntity.setStock(stockEntity.getStock() + quantity);
//                productStockRepository.save(stockEntity);

                // 예약 재고가 0이 되면 Redis에서 삭제
//                if (reservedStock - quantity == 0) {
//                    redisService.delete(reservedKey);
//                }
            } else {
                throw new VitaQueueException(ErrorCode.LOCK_ACQUISITION_FAILED, "재고 복원 중 락 획득 실패");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("재고 복원 중 오류 발생", e);
        } finally {
            lock.unlock(); // 락 해제
        }
    }


    // Redis 데이터를 주기적으로 DB에 반영
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void syncRedisToDatabase() {
        List<String> keys = redisService.getKeys("productStock:*");

        for (String key : keys) {
            Long productId = Long.valueOf(key.split(":")[1]);
            Integer stock = redisService.getValue(key);

            ProductStockEntity stockEntity = productStockRepository.findByProductIdWithLock(productId);
            stockEntity.setStock(stock);
        }

        productStockRepository.flush();
    }

    //    public boolean reserveStock(Long productId, Integer quantity) {
//        String redisKey = "reservedStock:" + productId;
//        RLock lock = redissonClient.getLock(redisKey);
//        System.out.println("11111111111111111111111111111111111111111111111111111111");
//        try {
//            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
//                System.out.println("222222222222222222222222222222222222222222222222");
//                Integer stock = redisService.getValue(redisKey);
//                System.out.println("33333333333333333333333333333333333333333333");
//                if (stock == null) {
//                    ProductStockEntity stockEntity = productStockRepository.findByProductId(productId)
//                            .orElseThrow(() -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND));
//                    stock = stockEntity.getStock();
//                    redisService.setValues(redisKey, stock);
//                }
//
//                if (stock < quantity) {
//                    return false;
//                }
//
//                redisService.decrement(redisKey, quantity);
//                redisService.setExpire(redisKey, 10, TimeUnit.MINUTES);
//                return true;
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Failed to acquire lock for stock reservation", e);
//        } finally {
//            lock.unlock();
//        }
//
//        return false;
//    }
    public boolean reserveStock(Long productId, Integer quantity) {
        String lockKey = "lock:reservedStock:" + productId; // 락 전용 키
        String redisKey = "reservedStock:" + productId; // 데이터 전용 키
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                System.out.println("Lock acquired successfully for key: " + lockKey);

                Integer stock = redisService.getValue(redisKey);
                System.out.println("Stock value retrieved from Redis: " + stock);

                if (stock == null) {
                    System.out.println("Stock not found in Redis, checking DB...");
                    ProductStockEntity stockEntity = productStockRepository.findByProductId(productId)
                            .orElseThrow(() -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND));
                    stock = stockEntity.getStock();
                    redisService.setValues(redisKey, stock);
                    System.out.println("Redis Key Set: " + redisKey + " = " + stock);
                }

                if (stock < quantity) {
                    System.out.println("Insufficient stock for key: " + redisKey);
                    return false;
                }

                redisService.decrement(redisKey, quantity);
                redisService.setExpire(redisKey, 10, TimeUnit.MINUTES);
                System.out.println("Reserved stock successfully for key: " + redisKey);
                return true;
            } else {
                System.out.println("Failed to acquire lock for key: " + lockKey);
                return false;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire lock for stock reservation", e);
        } finally {
            lock.unlock();
        }
    }

}
