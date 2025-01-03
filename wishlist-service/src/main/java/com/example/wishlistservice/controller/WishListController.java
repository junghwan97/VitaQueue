package com.example.wishlistservice.controller;


import com.example.wishlistservice.dto.request.WishProductRequest;
import com.example.wishlistservice.dto.request.WishProductUpdateRequest;
import com.example.wishlistservice.dto.response.WishProductResponse;
import com.example.wishlistservice.service.WishProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class WishListController {

    private final WishProductService wishProductService;

    @PostMapping("/wishList")
    public ResponseEntity<String> postWishProduct(@Valid @RequestBody WishProductRequest request, @RequestHeader("X-User-Id") String userId) {
        wishProductService.postWishProduct(request, Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body("장바구니에 등록되었습니다.");
    }

    @PatchMapping("/wishList")
    public ResponseEntity<String> updateWishProduct(@Valid @RequestBody WishProductUpdateRequest request) {
        wishProductService.updateWishProduct(request);
        return ResponseEntity.status(HttpStatus.OK).body("장바구니가 수정되었습니다.");
    }

    @DeleteMapping("/wishList/{wishProductId}")
    public ResponseEntity<String> deleteWishProduct(@PathVariable Long wishProductId, @RequestHeader("X-User-Id") String userId) {
        wishProductService.deleteWishProduct(wishProductId, Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body("장바구니에서 해당 상품이 삭제되었습니다.");
    }

    @GetMapping("/wishList")
    public ResponseEntity<List<WishProductResponse>> getWishProduct(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(wishProductService.getWishProducts(Long.valueOf(userId)));
    }
}
