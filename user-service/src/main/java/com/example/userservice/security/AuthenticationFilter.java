package com.example.userservice.security;

import com.example.userservice.dto.request.UserLoginRequest;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private UserService userService;
    private Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserService userService,
                                Environment env) {
        super.setAuthenticationManager(authenticationManager);
        this.userService = userService;
        this.env = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            UserLoginRequest user = new ObjectMapper().readValue(request.getInputStream(), UserLoginRequest.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String email = (((User)authResult.getPrincipal()).getUsername());
        UserEntity userDetails = userService.getUserDetailsByEmail(email);
        String token = "Bearer " + Jwts.builder()
                .setSubject(String.valueOf(userDetails.getId()))
                .claim("userId", userDetails.getId())
                .claim("auth", userDetails.getRole().getAuthority()) // 사용자 권한
                .setExpiration(new Date(System.currentTimeMillis() + Long.valueOf(env.getProperty("jwt.expiration-time"))))
                .signWith(SignatureAlgorithm.HS256, env.getProperty("jwt.secret-key"))
                .compact();
        response.addHeader("token", token);
        response.addHeader("userId", String.valueOf(userDetails.getId()));

    }
}
