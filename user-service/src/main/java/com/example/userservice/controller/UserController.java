package com.example.userservice.controller;


import com.example.userservice.dto.request.MailRequest;
import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.dto.request.UserUpdatePwRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserInfoResponse;
import com.example.userservice.dto.response.UserJoinResponse;
import com.example.userservice.service.MailService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/")
public class UserController {
    private final UserService userService;
    private final MailService mailService;

    @Autowired
    public UserController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

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
    // 회원 가입
    @PostMapping("/users")
    public ApiResponse<UserJoinResponse> createUser(@RequestBody UserJoinRequest user) throws Exception {
        UserJoinResponse userJoinResponse = userService.join(user);
        return ApiResponse.success(userJoinResponse);
    }

    // 사용자 상세 정보 조회
    @GetMapping("/users")
    public ApiResponse<UserInfoResponse> getUser(@RequestHeader("X-User-Id") String userId) throws Exception {
        UserInfoResponse returnValue = userService.getUserInfo(Long.valueOf(userId));
        return ApiResponse.success(returnValue);
    }

    // 사용자 상세 정보 수정
    @PatchMapping("/users")
    public ApiResponse<String> updateUserInfo(@Valid @RequestBody UserUpdateRequest request, @RequestHeader("X-User-Id") String userId) throws Exception {
        userService.updateUserInfo(request, Long.valueOf(userId));
        return ApiResponse.success("정보가 수정되었습니다.");
    }

    // 비밀번호 수정
    @PatchMapping("/pw")
    public ApiResponse<String> updatePassword(@Valid @RequestBody UserUpdatePwRequest request, @RequestHeader("X-User-Id") String userId) {
        userService.updatePassword(request, Long.valueOf(userId));
        return ApiResponse.success("비밀번호가 수정되었습니다.");
    }
}
