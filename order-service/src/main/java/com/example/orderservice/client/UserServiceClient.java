package com.example.orderservice.client;

import com.example.orderservice.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    // 사용자 상세 정보 조회
    @GetMapping("/users")
    UserInfoResponse userInfo(@RequestHeader("X-User-Id") String userId);
}