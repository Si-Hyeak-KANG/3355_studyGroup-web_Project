package com.study3355.account;

import com.study3355.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    // JPA persist, JPA detached 상태가 주는 영향(버그) -> detached 객체는 떨어져 있는 객체
    // save가 끝난 이후이기 때문에 Transactional 범위를 벗어난 상태
    @Transactional
    public void processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
    }

    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) // TODO encoding
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        return accountRepository.save(account);
    }

    // 메서드로 빼는 기능 -> Ctrl + Alt M
    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("삼삼오오, 회원 가입 인증"); // 메일의 제목
        mailMessage.setText(
                "/check-email-token?token=" + newAccount.getEmailCheckToken()
                        + "&email=" + newAccount.getEmail()
        ); // 메일의 본문

        javaMailSender.send(mailMessage); // 심플 메일 전송
    }


}
