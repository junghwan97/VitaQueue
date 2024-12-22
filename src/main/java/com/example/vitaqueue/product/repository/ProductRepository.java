package com.example.vitaqueue.product.repository;

import com.example.vitaqueue.product.model.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT p FROM ProductEntity p " +
            "ORDER BY p.id DESC " +
            "LIMIT :size ")
    List<ProductEntity> findAllOrderByIdDesc(Integer size);


    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.id < :cursorId " +
            "ORDER BY p.id DESC " +
            "LIMIT :size ")
    List<ProductEntity> findAllOrderByIdDescWithCursor(Long cursorId, Integer size);
}
