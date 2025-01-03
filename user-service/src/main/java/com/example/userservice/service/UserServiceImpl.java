package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.dto.request.UserUpdatePwRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.OrderProductResponse;
import com.example.userservice.dto.response.UserInfoResponse;
import com.example.userservice.dto.response.UserJoinResponse;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.exception.VitaQueueException;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.util.EncryptionUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;
    OrderServiceClient orderServiceClient;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           OrderServiceClient orderServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderServiceClient = orderServiceClient;

    }

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String encryptMail = EncryptionUtil.encrypt(username);
        UserEntity user = userRepository.findByEmail(encryptMail);
        if (user == null) throw new VitaQueueException(ErrorCode.USER_NOT_FOUND);

        return new User(user.getEmail(), user.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }


    @Override
    public UserJoinResponse join(UserJoinRequest requestDto) throws Exception {
        // 해당 메일로 회원가입한 유저가 있는지 확인
        UserEntity existedUserEntity = userRepository.findByEmail(EncryptionUtil.encrypt(requestDto.getEmail()));

        if (existedUserEntity != null)
            throw new VitaQueueException(ErrorCode.DUPLICATE_EMAIL, String.format("%s은 이미 등록된 메일입니다.", requestDto.getEmail()));

        // 개인 정보 암호화
        requestDto.encryptSensitiveData();
        // 비밀번호 암호화
        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        // 없다면 회원가입 로직 진행
        UserEntity userEntity = userRepository.save(UserEntity.of(requestDto, encodePassword));
        return UserJoinResponse.fromUserEntity(userEntity);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) throws Exception {
        UserEntity userEntity = getUserEntity(userId);
        ResponseEntity<List<OrderProductResponse>> orders = orderServiceClient.getOrders(userId);
        // orders가 null인 경우 빈 리스트로 초기화
        List<OrderProductResponse> orderList = (orders != null && orders.getBody() != null) ? orders.getBody() : Collections.emptyList();

        return UserInfoResponse.fromEntity(UserEntity.decryptSensitiveData(userEntity), orderList);
    }

    @Override
    @Transactional
    public void updateUserInfo(UserUpdateRequest request, Long userId) throws Exception {
        // 유저가 있는지 확인 / 없으면 예외 처리
        UserEntity userEntity = getUserEntity(userId);
        if (request.getAddress() != null) userEntity.setAddress(EncryptionUtil.encrypt(request.getAddress()));
        if (request.getPhone() != null) userEntity.setPhone(EncryptionUtil.encrypt(request.getPhone()));
    }

    @Override
    @Transactional
    public void updatePassword(UserUpdatePwRequest request, Long userId) {
        // 유저가 있는지 확인 / 없으면 예외 처리
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    @Override
    public UserEntity getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) throw new VitaQueueException(ErrorCode.USER_NOT_FOUND);
        return userEntity;
    }

    //유저 확인
    @Override
    public UserEntity getUserEntity(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new VitaQueueException(ErrorCode.USER_NOT_FOUND, "등록되지 않은 메일입니다."));
        return userEntity;
    }

}
