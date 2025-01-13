package com.example.userservice.service;

import com.example.userservice.dto.request.UserUpdatePwRequest;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.exception.VitaQueueException;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPasswordService {

    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserPasswordService(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void updatePassword(UserUpdatePwRequest request, Long userId) {
        UserEntity userEntity = getUserEntity(userId);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    //유저Id로 유저 확인
    public UserEntity getUserEntity(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) throw new VitaQueueException(ErrorCode.USER_NOT_FOUND);
        return userEntity;
    }
}
