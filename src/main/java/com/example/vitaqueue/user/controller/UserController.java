package com.example.vitaqueue.user.controller;

import com.example.vitaqueue.common.ApiResponse;
import com.example.vitaqueue.user.dto.request.MailRequest;
import com.example.vitaqueue.user.dto.request.UserJoinRequest;
import com.example.vitaqueue.user.dto.request.UserUpdatePwRequest;
import com.example.vitaqueue.user.dto.request.UserUpdateRequest;
import com.example.vitaqueue.user.dto.response.UserInfoResponse;
import com.example.vitaqueue.user.dto.response.UserJoinResponse;
import com.example.vitaqueue.user.service.MailService;
import com.example.vitaqueue.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MailService mailService;

    // 이메일 중복 확인 후 인증메일 전송
    @PostMapping("/sendCode")
    public ApiResponse<String> validateAndSendCode(@Valid @RequestBody MailRequest request) throws Exception {
        return ApiResponse.success(mailService.sendEmail(request));
    }

    // 인증 코드 확인
    @PostMapping("/checkCode")
    public ApiResponse<String> checkCode(@Valid @RequestBody MailRequest request) {
        return ApiResponse.success(mailService.checkCode(request));
    }

    //회원가입
    @PostMapping("/join")
    public ApiResponse<UserJoinResponse> join(@Valid @RequestBody UserJoinRequest requestDto) throws Exception {
        UserJoinResponse userJoinResponse = userService.join(requestDto);
        return ApiResponse.success(userJoinResponse);
    }

    // 사용자 상세 정보 조회
    @GetMapping("/userInfo")
    public ApiResponse<UserInfoResponse> userInfo(Authentication authentication) throws Exception {
        return ApiResponse.success(userService.getUserInfo(authentication.getName()));
    }

    // 사용자 상세 정보 수정
    @PatchMapping("/userInfo")
    public ApiResponse<Void> updateUserInfo(@Valid @RequestBody UserUpdateRequest request, Authentication authentication) throws Exception {
        userService.updateUserInfo(request, authentication.getName());
        return ApiResponse.success();
    }

    // 비밀번호 수정
    @PatchMapping("/pw")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UserUpdatePwRequest request, Authentication authentication) {
        userService.updatePassword(request, authentication.getName());
        return ApiResponse.success();
    }
}
