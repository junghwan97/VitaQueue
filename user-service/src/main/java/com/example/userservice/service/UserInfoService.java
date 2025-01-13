package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.OrderProductResponse;
import com.example.userservice.dto.response.UserInfoResponse;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.exception.VitaQueueException;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserInfoService {

    UserRepository userRepository;
    OrderServiceClient orderServiceClient;

    @Autowired
    public UserInfoService(UserRepository userRepository,
                           OrderServiceClient orderServiceClient) {
        this.userRepository = userRepository;
        this.orderServiceClient = orderServiceClient;
    }

    public UserInfoResponse getUserInfo(Long userId) throws Exception {
        // 회원 정보와 주문 목록 반환
        UserEntity userEntity = getUserEntity(userId);

        List<OrderProductResponse> orders = orderServiceClient.getOrderByUser(userId).getResult();
        List<OrderProductResponse> orderList = (orders != null) ? orders : Collections.emptyList();

        return UserInfoResponse.fromEntity(UserEntity.decryptSensitiveData(userEntity), orderList);
    }

    @Transactional
    public void updateUserInfo(UserUpdateRequest request, Long userId) throws Exception {
        UserEntity userEntity = getUserEntity(userId);

        if (request.getAddress() != null) userEntity.setAddress(EncryptionUtil.encrypt(request.getAddress()));
        if (request.getPhone() != null) userEntity.setPhone(EncryptionUtil.encrypt(request.getPhone()));
    }

    //유저Id로 유저 확인
    public UserEntity getUserEntity(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) throw new VitaQueueException(ErrorCode.USER_NOT_FOUND);
        return userEntity;
    }
}
