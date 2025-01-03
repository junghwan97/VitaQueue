package com.example.userservice.service;


import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.dto.request.UserUpdatePwRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.UserInfoResponse;
import com.example.userservice.dto.response.UserJoinResponse;
import com.example.userservice.jpa.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserJoinResponse join(UserJoinRequest requestDto) throws Exception;
    UserInfoResponse getUserInfo(Long userId) throws Exception;
    void updateUserInfo(UserUpdateRequest request, Long userId) throws Exception;
    void updatePassword(UserUpdatePwRequest request, Long userId);
    UserEntity getUserEntity(Long userId);
    UserEntity getUserDetailsByEmail(String email);
}
