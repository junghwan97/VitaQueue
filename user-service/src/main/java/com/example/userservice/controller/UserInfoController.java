package com.example.userservice.controller;

import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserInfoResponse;
import com.example.userservice.service.UserInfoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserInfoController {

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    // 사용자 상세 정보 조회
    @GetMapping("/users")
    public ApiResponse<UserInfoResponse> getUser(@RequestHeader("X-User-Id") String userId) throws Exception {
        UserInfoResponse returnValue = userInfoService.getUserInfo(Long.valueOf(userId));
        return ApiResponse.success(returnValue);
    }

    // 사용자 상세 정보 수정
    @PatchMapping("/users")
    public ApiResponse<String> updateUserInfo(@Valid @RequestBody UserUpdateRequest request, @RequestHeader("X-User-Id") String userId) throws Exception {
        userInfoService.updateUserInfo(request, Long.valueOf(userId));
        return ApiResponse.success("정보가 수정되었습니다.");
    }

}
