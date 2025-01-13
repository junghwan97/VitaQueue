package com.example.userservice.controller;

import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserJoinResponse;
import com.example.userservice.service.UserJoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserJoinController {

    private final UserJoinService userJoinService;

    @Autowired
    public UserJoinController(UserJoinService userJoinService) {
        this.userJoinService = userJoinService;
    }

    // 회원 가입
    @PostMapping("/users")
    public ApiResponse<UserJoinResponse> createUser(@RequestBody UserJoinRequest user) throws Exception {
        UserJoinResponse userJoinResponse = userJoinService.join(user);
        return ApiResponse.success(userJoinResponse);
    }

}
