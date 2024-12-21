package com.example.vitaqueue.user.dto.response;

import com.example.vitaqueue.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String name;
    private String address;
    private String phone;

    public static UserInfoResponse fromEntity(UserEntity user) {

        return new UserInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getAddress(),
                user.getPhone()
        );
    }

}
