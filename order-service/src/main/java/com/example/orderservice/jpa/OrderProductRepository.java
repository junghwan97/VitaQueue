package com.example.orderservice.jpa;


import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {
    List<OrderProductEntity> findByOrder(OrderEntity order);

    List<OrderProductEntity> findByOrderId(Long orderId);

    List<OrderProductEntity> findAllByStatusAndRegisteredAtBefore(OrderStatus orderStatus, Timestamp timestamp);
}
