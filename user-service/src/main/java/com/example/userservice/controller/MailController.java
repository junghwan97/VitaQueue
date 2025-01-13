package com.example.userservice.controller;

import com.example.userservice.dto.request.MailRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.service.MailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MailController {
    private final MailService mailService;

    @Autowired
    public MailController(MailService mailService) {
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
}