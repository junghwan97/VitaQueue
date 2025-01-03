package com.example.wishlistservice.repository;

import com.example.wishlistservice.model.entity.WishProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishProductRepository extends JpaRepository<WishProduct, Long> {
    Optional<WishProduct> findByUserIdAndProductId(Long userId, Long productId);

    Optional<WishProduct> findByIdAndUserId(Long wishProductId, Long id);

    List<WishProduct> findAllByUserId(Long userId);
}
