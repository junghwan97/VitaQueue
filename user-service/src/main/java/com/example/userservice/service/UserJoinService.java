package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.dto.response.UserJoinResponse;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.exception.VitaQueueException;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.util.EncryptionUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Service
public class UserJoinService implements UserDetailsService {
    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;
    OrderServiceClient orderServiceClient;

    @Autowired
    public UserJoinService(UserRepository userRepository,
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

    // userDetails 생성 로직
    private static User createUserDetails(UserEntity userEntity) {
        return new User(userEntity.getEmail(), userEntity.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }

    // 메일 암호화 후 조회
    private static String getEncryptMail(String username)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return EncryptionUtil.encrypt(username);
    }

    //유저 mail로 유저 확인
    public UserEntity getUserDetailsByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
