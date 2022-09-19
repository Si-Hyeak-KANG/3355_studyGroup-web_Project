package com.study3355.account;

import com.study3355.domain.Account;
import com.study3355.domain.Tag;
import com.study3355.settings.form.Notifications;
import com.study3355.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 데이터 변경은 서비스 계층으로 위임해서 트랜잭션 안에서 처리
 * 데이터 조회는 레파지톨리 또는 서비스 사용
 * 데이터를 조회하는 것은 굳이 트랜잭션이 없어도 됨. 그래서 view를 렌더링할때 LAZY 로딩 가능
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // manager 주입을 받기위해선 security config 설정을 바꿔줘야함.
    //private final AuthenticationManager authenticationManager;

    // JPA persist, JPA detached 상태가 주는 영향(버그) -> detached 객체는 떨어져 있는 객체
    // save가 끝난 이후이기 때문에 Transactional 범위를 벗어난 상태
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

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {

        Account account = accountRepository.findByEmail(emailOrUsername);

        if(account == null) {
            account = accountRepository.findByNickname(emailOrUsername);
        }

        if(account == null) {
            throw new UsernameNotFoundException(emailOrUsername);
        }

        // Principal 에 해당하는 객체 반환
        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {

        modelMapper.map(profile,account);

        /*account.setUrl(profile.getUrl());
        account.setBio(profile.getBio());
        account.setLocation(profile.getLocation());
        account.setOccupation(profile.getOccupation());
        // TODO 프로필 이미지
        account.setProfileImage(profile.getProfileImage());*/
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));

        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        /*account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());*/

        // 하지만 modelMapper 가 가끔 카멜표기 같은 필드이름들을 잘 못찾는 경우가 있음
       modelMapper.map(notifications,account);

       // 따라서 몇 가지 설정 필요
        ModelMapper modelMapper = new ModelMapper();

        // 이름이 underscore 가 아니면 하나의 프로퍼티로 간주
        modelMapper.getConfiguration()
                        .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
                                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);

        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디 올래, 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        javaMailSender.send(mailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId()); // EAGER fetch
        byId.ifPresent(a -> a.getTags().add(tag));

        // repository.getOne() -> LAZY 로딩, 필요한 순간에만 읽어옴.
    }
}
