package com.example.vitaqueue.user.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.security.EncryptionUtil;
import com.example.vitaqueue.user.dto.request.MailRequest;
import com.example.vitaqueue.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final RedisService redisService;


    public String sendEmail(@Valid MailRequest request) throws Exception {
        // 요청 값의 메일을 암호화 후 기존에 등록된 메일인지 확인 -> 존재하면 예외 처리
        if (userRepository.existsByEmail(EncryptionUtil.encrypt(request.getEmail()))) {
            throw new VitaQueueException(ErrorCode.DUPLICATE_EMAIL, "이미 등록된 메일입니다.");
        }
        // 등록되지 않은 메일이라면 인증 코드 생성 후 메일 전송
        ArrayList<String> toUserList = new ArrayList<>();
        toUserList.add(request.getEmail());
        int toUsersize = toUserList.size();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo((String[]) toUserList.toArray(new String[toUsersize]));
        // 인증 코드를 포함한 메일 내용 작성
        Random random = new Random();
        String code = String.valueOf(random.nextInt(900000) + 100000);
        simpleMailMessage.setSubject("안녕하세요! VitaQueue 인증메일입니다!");
        simpleMailMessage.setText("안녕하세요! VitaQueue 인증메일 보내드립니다.\n"
                + "=======================\n" + "인증번호 : " + code + "\n======================="
                + "\n 항상 저희 VitaQueue에 관심을 가져주셔서 감사합니다!");
        // 메일 전송
        javaMailSender.send(simpleMailMessage);
        // Redis에 인증 코드(key)와 이메일(value)를 저장, 유효 시간 설정
        redisService.setValues(code, request.getEmail(), Duration.ofMinutes(5));
        return code;
    }

    public String checkCode(MailRequest request) {
        // 입력 받은 인증 코드와 이메일을 Redis의 정보와 비교하여 일치 여부 확인
        String mail = redisService.getValue(request.getEmailCode());
        if (request.getEmail().equals(mail)) return "인증되었습니다.";
        else return "인증이 실패하였습니다.";
    }
}
