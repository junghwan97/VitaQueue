package com.example.productservice.jpa;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStockEntity, Long> {
    Optional<ProductStockEntity>findByProductId(Long productId); // 기존 재고 조회(Lock x)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductStockEntity p WHERE p.productId = :productId")
    ProductStockEntity findByProductIdWithLock(@Param("productId") Long productId);
}
