package com.example.vitaqueue.wishList.repository;

import com.example.vitaqueue.wishList.model.entity.WishProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishProductRepository extends JpaRepository<WishProduct, Long> {
    Optional<WishProduct> findByUserIdAndProductId(Long userId, Long productId);
}
