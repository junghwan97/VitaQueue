package com.example.userservice.controller;


import com.example.userservice.dto.request.MailRequest;
import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.dto.request.UserUpdatePwRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.UserInfoResponse;
import com.example.userservice.dto.response.UserJoinResponse;
import com.example.userservice.service.MailService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/")
public class UserController {
    private Environment env;
    private final UserService userService;
    private final MailService mailService;

    @Autowired
    public UserController(Environment env, UserService userService, MailService mailService) {
        this.env = env;
        this.userService = userService;
        this.mailService = mailService;
    }

    // 이메일 중복 확인 후 인증메일 전송
    @PostMapping("/sendCode")
    public ResponseEntity<String> validateAndSendCode(@Valid @RequestBody MailRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(mailService.sendEmail(request));
    }

    // 인증 코드 확인
    @PostMapping("/checkCode")
    public ResponseEntity<String> checkCode(@Valid @RequestBody MailRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(mailService.checkCode(request));
    }

    @PostMapping("/users")
    public ResponseEntity<UserJoinResponse> createUser(@RequestBody UserJoinRequest user) throws Exception {
        UserJoinResponse userJoinResponse = userService.join(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userJoinResponse);
    }

    // 사용자 상세 정보 조회
    @GetMapping("/users")
    public ResponseEntity<UserInfoResponse> getUser(@RequestHeader("X-User-Id") String userId) throws Exception {
        UserInfoResponse returnValue = userService.getUserInfo(Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

    // 사용자 상세 정보 수정
    @PatchMapping("/users")
    public ResponseEntity<String> updateUserInfo(@Valid @RequestBody UserUpdateRequest request, @RequestHeader("X-User-Id") String userId) throws Exception {
        userService.updateUserInfo(request, Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body("정보가 수정되었습니다.");
    }

    // 비밀번호 수정
    @PatchMapping("/pw")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody UserUpdatePwRequest request, @RequestHeader("X-User-Id") String userId) {
        userService.updatePassword(request, Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK).body("비밀번호가 수정되었습니다.");
    }
}
