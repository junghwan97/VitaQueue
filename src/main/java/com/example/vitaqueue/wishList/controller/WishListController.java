package com.example.vitaqueue.wishList.controller;

import com.example.vitaqueue.common.ApiResponse;
import com.example.vitaqueue.wishList.dto.request.WishProductRequest;
import com.example.vitaqueue.wishList.dto.request.WishProductUpdateRequest;
import com.example.vitaqueue.wishList.service.WishProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishProduct")
public class WishListController {

    private final WishProductService wishProductService;

    @PostMapping
    public ApiResponse<Void> postWishProduct(@Valid @RequestBody WishProductRequest request, Authentication authentication) {
        wishProductService.postWishProduct(request, authentication);
        return ApiResponse.success();
    }

    @PatchMapping
    public ApiResponse<Void> updateWishProduct(@Valid @RequestBody WishProductUpdateRequest request) {
        wishProductService.updateWishProduct(request);
        return ApiResponse.success();
    }

    @DeleteMapping("{wishProductId}")
    public ApiResponse<Void> deleteWishProduct(@PathVariable Long wishProductId) {
        wishProductService.deleteWishProduct(wishProductId);
        return ApiResponse.success();
    }
}
