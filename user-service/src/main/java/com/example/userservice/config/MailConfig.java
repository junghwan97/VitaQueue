package com.example.userservice.config;


import com.example.userservice.jpa.UserRepository;
import com.example.userservice.service.MailService;
import com.example.userservice.service.RedisService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.mail.enabled", havingValue = "true", matchIfMissing = false)
    public MailService mailService(JavaMailSender mailSender, UserRepository userRepository, RedisService redisService) {
        return new MailService(mailSender, userRepository, redisService);
    }
}