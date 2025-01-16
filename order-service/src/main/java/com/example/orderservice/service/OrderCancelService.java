package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderCancelService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductServiceClient productService;

    public OrderCancelService(OrderRepository orderRepository,
                              OrderProductRepository orderProductRepository,
                              ProductServiceClient productService) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보를 조회
        OrderEntity order = getOrderInfo(orderId);

        // 주문에 대한 권한을 확인
        if (!checkUserId(userId, order)) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }

        // 주문 상태를 확인
        if (checkOrderStatus(order)) {
            throw new VitaQueueException(ErrorCode.ORDER_CANCELLATION_NOT_ALLOWED, "배송 중 이전에만 주문을 취소할 수 있습니다.");
        }

        // 주문 상태를 취소 완료로 변경
        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 해당 주문과 연결된 상품들을 처리
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);
        for (OrderProductEntity orderProduct : orderProducts) {
            // 상품 상태를 취소 완료로 변경
            orderProduct.updateStatus(OrderStatus.CANCELLED);

            // 상품의 재고를 복구
            productService.increaseStock(orderProduct.getProductId(), orderProduct.getQuantity());
        }
    }

    // 주문 조회
    private OrderEntity getOrderInfo(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) throw new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다.");
        return order;
    }

    private static boolean checkUserId(Long userId, OrderEntity order) {
        return order.getUserId().equals(userId);
    }

    private static boolean checkOrderStatus(OrderEntity order) {
        return order.getStatus() != OrderStatus.PAYMENT_SUCCESS;
    }
}
