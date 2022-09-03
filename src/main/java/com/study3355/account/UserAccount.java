package com.study3355.account;

import com.study3355.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

// 스프링 시큐리티가 다루는 유저정보와 도메인이 다루는 유저정보의 갭을 매꿔줄 어댑터
// principal 객체로 사용
@Getter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
