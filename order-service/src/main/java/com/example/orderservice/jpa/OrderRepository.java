package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface OrderRepository extends CrudRepository<OrderEntity, Long> {
    //    OrderEntity findByOrderId(Long orderId);
    List<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findAllByStatusAndRegisteredAtBefore(OrderStatus status, Timestamp time);
}
