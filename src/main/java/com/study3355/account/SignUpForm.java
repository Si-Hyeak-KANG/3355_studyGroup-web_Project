package com.study3355.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 회원가입시 받아올 데이터 (postDto 같은 느낌)
 * 닉네임 (3~20자, 필수 입력)
 * 이메일 (이메일 형식, 필수 입력)
 * 패스워드 (8~50자, 필수 입력)
 */
@Data
public class SignUpForm {

    @NotNull
    @Length(min=3, max=20)
    @Pattern(regexp ="^[ㄱ-ㅎ가-힣A-Za-z0-9_-]{3,20}$") // 정규식 표현을 통해 문자(한글, 영어),숫자,기호(-,_)로만 3자이상 20자 이내만 허용
    private String nickname;

    @Email
    @NotNull
    private String email;

    @NotNull
    @Length(min=8, max=50)
    private String password;
}
