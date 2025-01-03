package com.example.userservice.dto.request;


import com.example.userservice.util.EncryptionUtil;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserUpdateRequest {

    @Size(min = 10, max = 100, message = "주소는 10~100자 사이로 입력해 주세요!")
    private String address;

    @Pattern(regexp = "^01[0-9]-?([0-9]{3,4})-?([0-9]{4})$", message = "전화번호는 01X-XXXX-XXXX 형식으로 입력해 주세요!")
    private String phone;

    public void encryptSensitiveData() throws Exception {
        this.address = EncryptionUtil.encrypt(this.address);
        this.phone = EncryptionUtil.encrypt(this.phone);
    }
}
