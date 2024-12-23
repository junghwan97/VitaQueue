package com.example.vitaqueue.wishList.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.product.model.entity.ProductEntity;
import com.example.vitaqueue.product.service.ProductService;
import com.example.vitaqueue.user.model.entity.UserEntity;
import com.example.vitaqueue.user.service.UserService;
import com.example.vitaqueue.wishList.dto.request.WishProductRequest;
import com.example.vitaqueue.wishList.dto.request.WishProductUpdateRequest;
import com.example.vitaqueue.wishList.dto.response.WishProductResponse;
import com.example.vitaqueue.wishList.model.entity.WishProduct;
import com.example.vitaqueue.wishList.repository.WishProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishProductService {

    private final ProductService productService;
    private final UserService userService;
    private final WishProductRepository wishProductRepository;

    public void postWishProduct(WishProductRequest request, Authentication authentication) {
        String email = authentication.getName();
        UserEntity userEntity = userService.getUserEntity(email);
        ProductEntity productEntity = productService.getProductEntity(request.getProductId());
        int quantity = request.getQuantity();

        // 장바구니에서 해당 상품이 이미 있는지 확인
        Optional<WishProduct> existingWishProduct = wishProductRepository.findByUserIdAndProductId(userEntity.getId(), productEntity.getId());

        if (existingWishProduct.isPresent()) {
            // 이미 있으면 수량 업데이트
            WishProduct item = existingWishProduct.get();
            item.setProductQuantity(item.getProductQuantity() + quantity);
            item.setProductPrice(item.getProductPrice().add(productEntity.getPrice().multiply(BigDecimal.valueOf(quantity))));
            wishProductRepository.save(item);
        } else {
            // 없으면 새로 추가
            WishProduct newItem = new WishProduct();
            newItem.setUserId(userEntity.getId());
            newItem.setProductId(productEntity.getId());
            newItem.setProductQuantity(quantity);
            newItem.setProductPrice(productEntity.getPrice().multiply(BigDecimal.valueOf(quantity)));
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
        BigDecimal price = existingWishProduct.getProductPrice()
                .divide(quantity, 2, RoundingMode.HALF_UP);
        existingWishProduct.setProductPrice(price.multiply(BigDecimal.valueOf(request.getQuantity())));
    }

    public void deleteWishProduct(Long wishProductId, Authentication authentication) {
        // 장바구니 주인 확인
        UserEntity userEntity = userService.getUserEntity(authentication.getName());
        // 장바구니에서 해당 상품이 이미 있는지 확인
        WishProduct existingWishProduct = wishProductRepository.findByIdAndUserId(wishProductId, userEntity.getId()).orElseThrow(
                () -> new VitaQueueException(ErrorCode.WISH_PRODUCT_NOT_FOUND, "장바구니에서 해당 상품을 찾지 못하였습니다.")
        );
        wishProductRepository.delete(existingWishProduct);
    }

    public List<WishProductResponse> getWishProducts(Authentication authentication) {
        String email = authentication.getName();
        UserEntity userEntity = userService.getUserEntity(email);
        return wishProductRepository.findAllByUserId(userEntity.getId()).stream().map(WishProductResponse::fromEntity).toList();
    }
}
