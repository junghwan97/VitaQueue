package com.example.wishlistservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WishProductUpdateRequest {

    @NotNull
    private Long wishProductId;

    @NotNull
    private Long productId;

    @NotNull
    private int quantity;

}
