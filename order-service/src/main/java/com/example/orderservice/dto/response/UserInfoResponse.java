package com.example.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String name;
    private String address;
    private String phone;

}
