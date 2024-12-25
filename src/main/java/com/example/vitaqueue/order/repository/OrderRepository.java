package com.example.vitaqueue.order.repository;

import com.example.vitaqueue.order.domain.entity.OrderEntity;
import com.example.vitaqueue.order.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByStatusAndRegisteredAtBefore(OrderStatus status, Timestamp time);
}
