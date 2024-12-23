package com.example.vitaqueue.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 50, message = "상품명은 50자 이하로 작성해 주세요")
    private String name;

    @Pattern(regexp = ".*", message = "가격을 입력해 주세요!")
    private BigDecimal price;

    @Size(max = 1000, message = "상품 설명은 1000자 이하로 작성해 주세요")
    private String descript;

}
