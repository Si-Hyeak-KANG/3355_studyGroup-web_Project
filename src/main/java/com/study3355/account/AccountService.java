package com.study3355.account;

import com.study3355.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    // manager 주입을 받기위해선 security config 설정을 바꿔줘야함.
    //private final AuthenticationManager authenticationManager;

    // JPA persist, JPA detached 상태가 주는 영향(버그) -> detached 객체는 떨어져 있는 객체
    // save가 끝난 이후이기 때문에 Transactional 범위를 벗어난 상태
    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
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


    public void login(Account account) {
        // 실제로 AuthenticationManager 와 같은 동작
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        new UserAccount(account),
                        account.getPassword(),
                        List.of(new SimpleGrantedAuthority("Role_USER")));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);

        // 정석 방법 - 쓰지 않는 이유: 우리는 Password text 를 db에 저장하지 않을거며, 사용하지 않을 거임, 즉 인코딩된 패스워드 사용
        /*        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password); // 사용자가 입력한 username,password
        Authentication authenticate = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authenticate);
        */

    }
}
