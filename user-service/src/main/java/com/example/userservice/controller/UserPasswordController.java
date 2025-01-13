package com.example.userservice.controller;

import com.example.userservice.dto.request.UserUpdatePwRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.service.UserPasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserPasswordController {

    private final UserPasswordService userPasswordService;

    @Autowired
    public UserPasswordController(UserPasswordService userPasswordService) {
        this.userPasswordService = userPasswordService;
    }

    // 비밀번호 수정
    @PatchMapping("/pw")
    public ApiResponse<String> updatePassword(@Valid @RequestBody UserUpdatePwRequest request, @RequestHeader("X-User-Id") String userId) {
        userPasswordService.updatePassword(request, Long.valueOf(userId));
        return ApiResponse.success("비밀번호가 수정되었습니다.");
    }
}
