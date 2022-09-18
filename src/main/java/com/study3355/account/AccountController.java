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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);


        // TODO 회원 가입 처리
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        // (리팩토링) 로직 메서드화를 통해 긴 코드를 간결화
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        /*// 현재 여기는 트랜잭션이 없음
        // 트랜잭션은 서비스에 위임해서 관리할것
        account.completeSignUp(); // (리팩토링)로직을 추가하므로써, sign up이 완료가 되는구나라고 바로 직관적으로 알 수 있음.
        accountService.login(account);

        // 객체의 변경사항이 db에 적용 안됨.
        // 영속성 컨텍스트 db에서 읽어오는 persist한 객체를 관리하는 객체*/

        accountService.completeSignUp(account);

        model.addAttribute("numberOfUser", accountRepository.count()); // 몇번째 유저인가
        model.addAttribute("nickname", account.getNickname());

        return view;
    }

    @GetMapping("/check-email")
    public String checkEmil(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if (byNickname == null) {
            throw new IllegalAccessError(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute("account", byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {

        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            //return "account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error","로그인할 수 없습니다.");
            return view;
        }
        accountService.login(account);
        return view;
    }


}
