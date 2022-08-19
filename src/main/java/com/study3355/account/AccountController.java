package com.study3355.account;

import com.study3355.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    // 바인딩 된 데이터를 받을 때, 유효성 검증 진행
    // ModelAttribute 의 타입과 InitBinder 의 문자열값과 동일
    @InitBinder("SignUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {

        // model.addAttribute("signUpForm",new SignUpForm());
        model.addAttribute(new SignUpForm()); // 생략 가능(클래스의 이름 camel 형태로 생성됨)

        return "account/sign-up";
    }

    // 1) JSR-303 유효성 검사를 위해서 애너테이션 @Valid 사용, Valid 와 Validated 차이는 뭘까?
    //validated 는 valid 의 기능을 포함
    // 2) 원래는 (복합)객체로 바인딩 받기 위해선 @ModelAttribute 사용, 생략 가능
    // 3) Errors 바인딩할 때 발생할 수 있는 에러 캐치
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm,
                               Errors errors) {
        // (유효성검사)에러가 있을 시, form 을 다시 보여줌
        if (errors.hasErrors()) {
            return "account/sign-up";
        } // front 단에서만 진행되는 유효성 검증은 뚫릴 수 있음.(javascript 조작) 따라서 서버단에서도 검증해야함

        accountService.processNewAccount(signUpForm);


        // TODO 회원 가입 처리
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if(account == null) {
            model.addAttribute("error","wrong.email");
            return view;
        }

        if(!account.getEmailCheckToken().equals(token)) {
            model.addAttribute("error","wrong.token");
            return view;
        }

        account.setEmailVerified(true); // 인증여부 true
        account.setJoinedAt(LocalDateTime.now()); // 가입 날짜 적용
        model.addAttribute("numberOfUser", accountRepository.count()); // 몇번째 유저인가
        model.addAttribute("nickname", account.getNickname());

        return view;
    }
}
