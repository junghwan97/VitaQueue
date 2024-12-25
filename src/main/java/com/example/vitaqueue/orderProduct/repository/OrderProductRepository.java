package com.example.vitaqueue.orderProduct.repository;

import com.example.vitaqueue.order.domain.entity.OrderEntity;
import com.example.vitaqueue.order.domain.enums.OrderStatus;
import com.example.vitaqueue.orderProduct.domain.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {
    List<OrderProductEntity> findByOrder(OrderEntity order);

    List<OrderProductEntity> findByOrderId(Long orderId);

    List<OrderProductEntity> findAllByStatusAndRegisteredAtBefore(OrderStatus orderStatus, Timestamp timestamp);
}
