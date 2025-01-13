package com.example.userservice.security;

import com.example.userservice.service.UserJoinService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurity {

    private final UserJoinService userService; // 사용자 서비스 (UserDetailsService 역할)
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 암호화 인코더
    private final Environment env; // 애플리케이션 환경 설정을 읽기 위한 환경 객체

    public WebSecurity(Environment env, UserJoinService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.env = env;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // Spring Security의 필터 체인을 설정하는 Bean
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        http.csrf(csrf -> csrf.disable());

        // 요청 URL에 따른 권한 설정
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/**").permitAll() // Actuator 엔드포인트는 인증 없이 접근 허용
                .requestMatchers("/health_check/**").permitAll() // Health Check 엔드포인트는 인증 없이 접근 허용
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers("/**").permitAll()
        );

        // 사용자 정의 인증 필터 추가
        http.addFilter(authenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class))));

        return http.build();
    }

    // AuthenticationManager Bean 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 사용자 정의 인증 필터 생성 메서드
    private AuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, userService, env);
        return authenticationFilter;
    }

    // 사용자 인증 및 비밀번호 인코더 설정
//    @Bean
    public void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }
}
