package com.example.vitaqueue.product.repository;

import com.example.vitaqueue.product.model.entity.ProductStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStockEntity, Long> {
}
