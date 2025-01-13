package com.example.productservice.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.isFlashSale = false " +
            "ORDER BY p.id DESC " +
            "LIMIT :size ")
    List<ProductEntity> findAllByIsFlashSaleFalseOrderByIdDesc(Integer size);


    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.isFlashSale = false " +
            "AND p.id < :cursorId " +
            "ORDER BY p.id DESC " +
            "LIMIT :size ")
    List<ProductEntity> findAllByIsFlashSaleFalseOrderByIdDescWithCursor(Long cursorId, Integer size);

    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.isFlashSale = true " +
            "ORDER BY p.id DESC " +
            "LIMIT :size ")
    List<ProductEntity> findAllByIsFlashSaleTrueOrderByIdDesc(Integer size);

    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.isFlashSale = true " +
            "AND p.id < :cursorId " +
            "ORDER BY p.id DESC " +
            "LIMIT :size ")
    List<ProductEntity> findAllByIsFlashSaleTrueOrderByIdDescWithCursor(Long cursorId, Integer size);

    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.startAt <= :currentTime " +
            "AND p.isFlashSale = true " +
            "AND p.isFlashSaleOpen = false")
    List<ProductEntity> findFlashSaleProducts(@Param("currentTime") Timestamp currentTime);
}
