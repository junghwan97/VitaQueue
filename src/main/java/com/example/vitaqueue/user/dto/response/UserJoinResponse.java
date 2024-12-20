package com.example.vitaqueue.user.dto.response;

import com.example.vitaqueue.user.model.enums.UserRole;
import com.example.vitaqueue.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Long id;
    private String userName;
    private UserRole role;

    public static UserJoinResponse fromUserEntity(UserEntity user) {

        return new UserJoinResponse(
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}