package com.example.vitaqueue.user.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.security.EncryptionUtil;
import com.example.vitaqueue.user.dto.request.UserJoinRequest;
import com.example.vitaqueue.user.dto.request.UserUpdatePwRequest;
import com.example.vitaqueue.user.dto.request.UserUpdateRequest;
import com.example.vitaqueue.user.dto.response.UserInfoResponse;
import com.example.vitaqueue.user.dto.response.UserJoinResponse;
import com.example.vitaqueue.user.model.entity.UserEntity;
import com.example.vitaqueue.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserJoinResponse join(UserJoinRequest requestDto) throws Exception {
        // 해당 메일로 회원가입한 유저가 있는지 확인
        userRepository.findByEmail(requestDto.getEmail()).ifPresent(it -> {
            throw new VitaQueueException(ErrorCode.DUPLICATE_EMAIL, String.format("%s은 이미 등록된 메일입니다.", requestDto.getEmail()));
        });
        // 개인 정보 암호화
        requestDto.encryptSensitiveData();
        // 비밀번호 암호화
        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        // 없다면 회원가입 로직 진행
        UserEntity userEntity = userRepository.save(UserEntity.of(requestDto, encodePassword));
        return UserJoinResponse.fromUserEntity(userEntity);
    }


    public UserInfoResponse getUserInfo(String email) throws Exception {
        // 유저가 있는지 확인 / 없으면 예외 처리
        UserEntity userEntity = getUserEntity(email);
        return UserInfoResponse.fromEntity(UserEntity.decryptSensitiveData(userEntity));
    }


    @Transactional
    public void updateUserInfo(UserUpdateRequest request, String email) throws Exception {
        // 유저가 있는지 확인 / 없으면 예외 처리
        UserEntity userEntity = getUserEntity(email);
        if (request.getAddress() != null) userEntity.setAddress(EncryptionUtil.encrypt(request.getAddress()));
        if (request.getPhone() != null) userEntity.setPhone(EncryptionUtil.encrypt(request.getPhone()));
    }

    @Transactional
    public void updatePassword(UserUpdatePwRequest request, String email) {
        // 유저가 있는지 확인 / 없으면 예외 처리
        UserEntity userEntity = getUserEntity(email);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    // 유저 확인
    public UserEntity getUserEntity(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new VitaQueueException(ErrorCode.USER_NOT_FOUND, "등록되지 않은 메일입니다."));
        return userEntity;
    }

}
