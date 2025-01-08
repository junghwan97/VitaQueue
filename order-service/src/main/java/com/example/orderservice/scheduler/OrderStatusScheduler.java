package com.example.orderservice.scheduler;

import com.example.orderservice.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Component
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class OrderStatusScheduler {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    /**
     * 매일 자정에 실행하여 주문 상태를 업데이트
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void updateOrderStatuses() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        // D+1: CREATED -> DELIVERY
        List<OrderEntity> toDeliveryOrders = orderRepository.findAllByStatusAndRegisteredAtBefore(OrderStatus.PAYMENT_SUCCESS, new Timestamp(now.getTime()));
        // 주문 상태 변경
        toDeliveryOrders.forEach(order -> {order.updateStatus(OrderStatus.DELIVERY);});
        orderRepository.saveAll(toDeliveryOrders);
        // 상세 주문 상태 변경
        List<OrderProductEntity> toDeliveryOrderProducts = orderProductRepository.findAllByStatusAndRegisteredAtBefore(OrderStatus.PAYMENT_SUCCESS, new Timestamp(now.getTime()));
        toDeliveryOrderProducts.forEach(orderProduct -> {orderProduct.updateStatus(OrderStatus.DELIVERY);});
        orderProductRepository.saveAll(toDeliveryOrderProducts);
        // D+2: DELIVERY -> DELIVERED
        List<OrderEntity> toDeliveredOrders = orderRepository.findAllByStatusAndRegisteredAtBefore(OrderStatus.DELIVERY, new Timestamp(now.getTime() - 24 * 60 * 60 * 1000));
        toDeliveredOrders.forEach(order -> {order.updateStatus(OrderStatus.DELIVERED);});
        // 상세 주문 상태 변경
        List<OrderProductEntity> toDeliveredOrderProducts = orderProductRepository.findAllByStatusAndRegisteredAtBefore(OrderStatus.DELIVERY, new Timestamp(now.getTime() - 24 * 60 * 60 * 1000));
        toDeliveredOrderProducts.forEach(orderProduct -> {orderProduct.updateStatus(OrderStatus.DELIVERED);});
        orderProductRepository.saveAll(toDeliveredOrderProducts);
        orderRepository.saveAll(toDeliveredOrders);
    }
}
