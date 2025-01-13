package com.example.wishlistservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WishProductRequest {
    @NotNull
    private Long productId;

    @NotNull
    private Integer quantity;

}
