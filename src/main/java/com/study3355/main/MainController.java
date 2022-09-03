package com.study3355.main;

import com.study3355.account.CurrentUser;
import com.study3355.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String  home(@CurrentUser Account account, Model model) {

        // 인증을한 사용자
        if (account != null ){
            model.addAttribute(account);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
