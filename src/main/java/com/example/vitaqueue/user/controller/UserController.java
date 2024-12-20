package com.example.vitaqueue.user.controller;

import com.example.vitaqueue.user.dto.request.MailRequest;
import com.example.vitaqueue.user.dto.request.UserJoinRequest;
import com.example.vitaqueue.common.ApiResponse;
import com.example.vitaqueue.user.dto.response.UserJoinResponse;
import com.example.vitaqueue.user.service.MailService;
import com.example.vitaqueue.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
