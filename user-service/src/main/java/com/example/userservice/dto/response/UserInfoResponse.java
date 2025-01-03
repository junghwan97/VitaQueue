package com.example.userservice.dto.response;


import com.example.userservice.jpa.UserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String name;
    private String address;
    private String phone;
    private List<OrderProductResponse> orders;



    public static UserInfoResponse fromEntity(UserEntity user, List<OrderProductResponse> orders) {
        return new UserInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getAddress(),
                user.getPhone(),
                orders
        );
    }

}
