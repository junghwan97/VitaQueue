package com.example.vitaqueue.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message = "상품을 선택해 주세요")
    private Long productId;
    @NotNull(message = "수량을 입력해 주세요")
    private int quantity;

}
