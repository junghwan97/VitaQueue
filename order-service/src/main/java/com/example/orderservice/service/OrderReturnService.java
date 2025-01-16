package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderReturnService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductServiceClient productService;

    public OrderReturnService(OrderRepository orderRepository,
                              OrderProductRepository orderProductRepository,
                              ProductServiceClient productService) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
    }

    @Transactional
    public void returnOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보 조회
        OrderEntity order = getOrderInfo(orderId);

        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userId)) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }

        // 주문 상태 확인
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new VitaQueueException(ErrorCode.INVALID_ORDER_STATE, "배송 완료 상태인 주문만 반품할 수 있습니다.");
        }

        // 반품 가능 기간 확인
        if (!withinReturnPeriod(order.getRegisteredAt())) {
            throw new VitaQueueException(ErrorCode.RETURN_PERIOD_EXPIRED, "반품 가능 기간이 지났습니다.");
        }

        // 주문 상태를 반품 요청으로 변경
        order.updateStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        // 해당 주문과 연결된 상품들을 처리
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);
        for (OrderProductEntity orderProduct : orderProducts) {
            // 상품 상태를 반품 완료로 변경
            orderProduct.updateStatus(OrderStatus.RETURNED);
            // 상품의 재고를 복구
            productService.increaseStock(orderProduct.getProductId(), orderProduct.getQuantity());
        }
    }

    private OrderEntity getOrderInfo(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) throw new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다.");
        return order;
    }

    private boolean withinReturnPeriod(Timestamp registeredAt) {
        return registeredAt != null && registeredAt.toLocalDateTime().plusDays(1).isAfter(LocalDateTime.now());
    }

}
