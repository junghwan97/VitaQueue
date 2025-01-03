package com.example.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message = "상품을 선택해 주세요")
    private Long productId;
    @NotNull(message = "수량을 입력해 주세요")
    private int quantity;
    @Size(min = 10, max = 100, message = "주소는 10~100자 사이로 입력해 주세요!")
    private String address;

    @Pattern(regexp = "^01[0-9]-?([0-9]{3,4})-?([0-9]{4})$", message = "전화번호는 01X-XXXX-XXXX 형식으로 입력해 주세요!")
    private String phone;

}
