package com.example.userservice.dto.response;


import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRole;
import com.example.userservice.util.EncryptionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Long id;
    private String userName;
    private UserRole role;

    public static UserJoinResponse fromUserEntity(UserEntity user) throws Exception{

        return new UserJoinResponse(
                user.getId(),
                EncryptionUtil.decrypt(user.getName()),
                user.getRole()
        );
    }
}