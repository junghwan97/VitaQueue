package com.example.vitaqueue.security;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.user.model.entity.UserEntity;
import com.example.vitaqueue.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String encryptMail = EncryptionUtil.encrypt(email);
        UserEntity user = userRepository.findByEmail(encryptMail)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.USER_NOT_FOUND));

        return new UserDetailsImpl(user);
    }
}
