package com.example.userservice.dto.request;


import com.example.userservice.util.EncryptionUtil;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserLoginRequest {
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "이메일 형식에 맞게 입력해 주세요!")
    private String email;

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 8~20자 사이이며, 영문, 숫자, 특수문자를 포함해야 합니다!")
    private String password;

    public void encryptSensitiveData() throws Exception {
        this.email = EncryptionUtil.encrypt(this.email);
    }

}