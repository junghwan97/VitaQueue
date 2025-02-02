package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductServiceClient productService;
    private final RedissonClient redissonClient;

    public PaymentService(OrderRepository orderRepository,
                          OrderProductRepository orderProductRepository,
                          ProductServiceClient productService,
                          RedissonClient redissonClient) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
        this.redissonClient = redissonClient;
    }

    @Transactional
    public String enterPayment(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new VitaQueueException(ErrorCode.INVALID_ORDER_FOR_PAYMENT, "결제를 위한 주문 상태가 아닙니다.");
        }

        // 20% 확률로 결제 진입 실패 (시뮬레이션)
        if (Math.random() < 0.2) {
            order.updateStatus(OrderStatus.PAYMENT_FAILED);
            restoreStockWithLock(order); // DB & Redis 재고 원복 (락 적용)
            log.info("결제 진입 단계에서 결제 실패, 재고 복구 수행");
            return "결제 진입 단계에서 결제 실패";
        }

        // 결제 진입 성공 → 상태 업데이트
        order.updateStatus(OrderStatus.PAYMENT_ENTERED);
        return "결제 진입 성공";
    }

    @Transactional
    public String processPayment(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.PAYMENT_ENTERED) {
            throw new VitaQueueException(ErrorCode.INVALID_ORDER_FOR_PAYMENT, "결제를 위한 주문 상태가 아닙니다.");
        }

        // 20% 확률로 결제 실패 (시뮬레이션)
        if (Math.random() < 0.2) {
            order.updateStatus(OrderStatus.PAYMENT_FAILED);
            restoreStockWithLock(order); // DB & Redis 재고 원복 (락 적용)
            log.info("결제 시도 단계에서 결제 실패, 재고 복구 수행");
            return "결제 시도 단계에서 결제 실패";
        }

        // 결제 성공 → 주문 상태 업데이트
        order.updateStatus(OrderStatus.PAYMENT_SUCCESS);

        // 주문 상품 리스트 조회
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);

        // 개별 상품 단위로 락을 걸고 재고 차감
        for (OrderProductEntity orderProduct : orderProducts) {
            String lockKey = "productStock:" + orderProduct.getProductId();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                    // 최종 재고 차감
                    productService.decreaseStock(orderProduct.getProductId(), orderProduct.getQuantity());
                    orderProduct.updateStatus(OrderStatus.PAYMENT_SUCCESS);
                } else {
                    throw new VitaQueueException(ErrorCode.LOCK_ACQUISITION_FAILED, "재고 감소 중 락 획득 실패");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("결제 처리 중 오류 발생", e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        orderRepository.save(order);
        return "결제가 완료되었습니다.";
    }


    // 주문 조회 (유효성 검증)
    private OrderEntity getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));
    }

    private void restoreStockWithLock(OrderEntity order) {
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);

        for (OrderProductEntity orderProduct : orderProducts) {
            String lockKey = "product-Stock:" + orderProduct.getProductId();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                    // Redis & DB 재고 복구
                    productService.increaseStock(orderProduct.getProductId(), orderProduct.getQuantity());
                    orderProduct.updateStatus(OrderStatus.PAYMENT_FAILED);
                } else {
                    log.warn("재고 복구 중 락 획득 실패: 상품 ID {}", orderProduct.getProductId());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("재고 복구 중 오류 발생", e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        orderProductRepository.saveAll(orderProducts);
    }

}
