package com.example.vitaqueue.user.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.jwt.JwtUtil;
import com.example.vitaqueue.user.dto.request.UserJoinRequest;
import com.example.vitaqueue.user.dto.response.UserJoinResponse;
import com.example.vitaqueue.user.model.entity.UserEntity;
import com.example.vitaqueue.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
}
