package com.example.vitaqueue.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductRequest {

    @Size(max = 50, message = "상품명은 50자 이하로 작성해 주세요")
    @NotBlank(message = "상품명을 입력해 주세요")
    private String name;

    @NotNull(message = "가격을 입력해 주세요!")
    private BigDecimal price;

    @Size(max = 1000, message = "상품 설명은 1000자 이하로 작성해 주세요")
    @NotBlank(message = "상품 설명을 입력해 주세요")
    private String descript;

    @NotNull(message = "상품의 카테고리를 입력해 주세요!")
    private String category;

    @NotNull(message = "상품의 재고를 입력해 주세요!")
    private Long stock;
}
