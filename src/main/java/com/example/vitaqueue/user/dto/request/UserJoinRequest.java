package com.example.vitaqueue.user.dto.request;


import com.example.vitaqueue.security.EncryptionUtil;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
//@AllArgsConstructor
public class UserJoinRequest {

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "이메일 형식에 맞게 입력해 주세요!")
    private String email;

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 8~20자 사이이며, 영문, 숫자, 특수문자를 포함해야 합니다!")
    private String password;

    @Pattern(regexp = "^[가-힣a-zA-Z]{2,30}$", message = "이름은 2~30자 사이의 한글 또는 영문만 가능합니다!")
    private String name;

    @Size(min = 10, max = 100, message = "주소는 10~100자 사이로 입력해 주세요!")
    private String address;

    @Pattern(regexp = "^01[0-9]-?([0-9]{3,4})-?([0-9]{4})$", message = "전화번호는 01X-XXXX-XXXX 형식으로 입력해 주세요!")
    private String phone;

    public void encryptSensitiveData() throws Exception {
        this.email = EncryptionUtil.encrypt(this.email);
        this.name = EncryptionUtil.encrypt(this.name);
        this.address = EncryptionUtil.encrypt(this.address);
        this.phone = EncryptionUtil.encrypt(this.phone);
    }
}