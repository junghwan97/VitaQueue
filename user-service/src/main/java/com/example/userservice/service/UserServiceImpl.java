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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
        String encryptMail = getEncryptMail(username);
        UserEntity userEntity = getUserDetailsByEmail(encryptMail);
        if (userEntity == null) throw new VitaQueueException(ErrorCode.USER_NOT_FOUND);

        return createUserDetails(userEntity);
    }

    @Override
    public UserJoinResponse join(UserJoinRequest requestDto) throws Exception {
        String encryptMail = getEncryptMail(requestDto.getEmail());
        UserEntity existedUserEntity = getUserDetailsByEmail(encryptMail);
        if (existedUserEntity != null)
            throw new VitaQueueException(ErrorCode.DUPLICATE_EMAIL, String.format("%s은 이미 등록된 메일입니다.", requestDto.getEmail()));

        requestDto.encryptSensitiveData();
        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        UserEntity userEntity = userRepository.save(UserEntity.of(requestDto, encodePassword));

        return UserJoinResponse.fromUserEntity(userEntity);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) throws Exception {
        // 회원 정보와 주문 목록 반환
        UserEntity userEntity = getUserEntity(userId);

        List<OrderProductResponse> orders = orderServiceClient.getOrders(userId).getResult();
        List<OrderProductResponse> orderList = (orders != null) ? orders : Collections.emptyList();

        return UserInfoResponse.fromEntity(UserEntity.decryptSensitiveData(userEntity), orderList);
    }

    @Override
    @Transactional
    public void updateUserInfo(UserUpdateRequest request, Long userId) throws Exception {
        UserEntity userEntity = getUserEntity(userId);

        if (request.getAddress() != null) userEntity.setAddress(EncryptionUtil.encrypt(request.getAddress()));
        if (request.getPhone() != null) userEntity.setPhone(EncryptionUtil.encrypt(request.getPhone()));
    }

    @Override
    @Transactional
    public void updatePassword(UserUpdatePwRequest request, Long userId) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    // 메일 암호화 후 조회
    private static String getEncryptMail(String username)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return EncryptionUtil.encrypt(username);
    }

    //유저 mail로 유저 확인
    @Override
    public UserEntity getUserDetailsByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // userDetails 생성 로직
    private static User createUserDetails(UserEntity userEntity) {
        return new User(userEntity.getEmail(), userEntity.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }

    //유저Id로 유저 확인
    @Override
    public UserEntity getUserEntity(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) throw new VitaQueueException(ErrorCode.USER_NOT_FOUND);
        return userEntity;
    }
}
