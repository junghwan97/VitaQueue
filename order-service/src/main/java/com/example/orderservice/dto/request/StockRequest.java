package com.example.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockRequest {

    @NotNull
    private Long productId;
    @NotNull
    @Min(1)
    private Integer stock;
}
