package com.study3355.account;

import lombok.Data;

/**
 * 회원가입시 받아올 데이터 (postDto 같은 느낌)
 */
@Data
public class SignUpForm {

    private String nickname;
    private String email;
    private String password;
}
