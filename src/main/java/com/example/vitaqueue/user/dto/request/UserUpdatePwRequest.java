package com.example.vitaqueue.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePwRequest {

    @NotNull(message = "비밀번호를 입력해 주세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이이며, 영문, 숫자, 특수문자를 포함해야 합니다!")
    private String password;
}
