package com.example.wishlistservice.service;


import com.example.wishlistservice.client.ProductServiceClient;
import com.example.wishlistservice.dto.request.WishProductRequest;
import com.example.wishlistservice.dto.request.WishProductUpdateRequest;
import com.example.wishlistservice.dto.response.ProductResponse;
import com.example.wishlistservice.dto.response.WishProductResponse;
import com.example.wishlistservice.exception.ErrorCode;
import com.example.wishlistservice.exception.VitaQueueException;
import com.example.wishlistservice.model.entity.WishProduct;
import com.example.wishlistservice.repository.WishProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishProductService {

    private final ProductServiceClient productServiceClient;
    private final WishProductRepository wishProductRepository;

    public void postWishProduct(WishProductRequest request, Long userId) {
        ProductResponse product = productServiceClient.getProduct(request.getProductId());
        // 장바구니에서 해당 상품이 이미 있는지 확인
        Optional<WishProduct> existingWishProduct = wishProductRepository.findByUserIdAndProductId(userId, request.getProductId());

        if (existingWishProduct.isPresent()) {
            // 이미 있으면 수량 업데이트
            WishProduct item = existingWishProduct.get();
            item.setProductQuantity(item.getProductQuantity() + request.getQuantity());
            item.setProductPrice(item.getProductPrice().add(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))));
            wishProductRepository.save(item);
        } else {
            // 없으면 새로 추가
            WishProduct newItem = new WishProduct();
            newItem.setUserId(userId);
            newItem.setProductId(request.getProductId());
            newItem.setProductQuantity(request.getQuantity());
            newItem.setProductPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            wishProductRepository.save(newItem);
        }
    }

    @Transactional
    public void updateWishProduct(WishProductUpdateRequest request) {
        // 장바구니에서 해당 상품이 이미 있는지 확인
        WishProduct existingWishProduct = wishProductRepository.findById(request.getWishProductId()).orElseThrow(
                () -> new VitaQueueException(ErrorCode.WISH_PRODUCT_NOT_FOUND, "장바구니에서 해당 상품을 찾지 못하였습니다.")
        );
        BigDecimal quantity = new BigDecimal(existingWishProduct.getProductQuantity());
        existingWishProduct.setProductQuantity(request.getQuantity());
        // 상품 가격
        BigDecimal price = existingWishProduct.getProductPrice().divide(quantity, 2, RoundingMode.HALF_UP);
        existingWishProduct.setProductPrice(price.multiply(BigDecimal.valueOf(request.getQuantity())));
    }

    public void deleteWishProduct(Long wishProductId, Long userId) {
        // 장바구니 주인 확인
        // 장바구니에서 해당 상품이 이미 있는지 확인
        WishProduct existingWishProduct = wishProductRepository.findByIdAndUserId(wishProductId, userId).orElseThrow(
                () -> new VitaQueueException(ErrorCode.WISH_PRODUCT_NOT_FOUND, "장바구니에서 해당 상품을 찾지 못하였습니다.")
        );
        wishProductRepository.delete(existingWishProduct);
    }

    public List<WishProductResponse> getWishProducts(Long userId) {
        return wishProductRepository.findAllByUserId(userId).stream().map(WishProductResponse::fromEntity).toList();
    }
}
