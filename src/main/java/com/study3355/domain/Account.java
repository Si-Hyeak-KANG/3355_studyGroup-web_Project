package com.study3355.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 : 계정
 *  1) 식별자
 *  // 로그인
 *  2) 이메일
 *  3) 닉네임
 *  4) 패스워드
 *  5) 이메일 인증 여부
 *  6) 이메일 인증 시 필요한 토큰
 *  // 프로필
 *  7) 가입 날짜
 *  8) 자기소개
 *  9) 링크
 *  10) 직업
 *  11) 거주지역
 *  12) 프로필사진
 *  // 알림 설정 여부
 *  13) 스터디가 생성 알림, 이메일 수령 여부
 *  14) 스터디가 생성 알림, 웹 수령 여부
 *  15) 스터디 가입 신청 결과, 이메일 수령 여부
 *  16) 스터디 가입 신청 결과, 웹 수령 여부
 *  17) 스터디 갱신 정보, 이메일 수령 여부
 *  18) 스터디 갱신 정보, 웹 수령 여부
 */
@Entity
@Getter @Setter @EqualsAndHashCode(of="id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    // 로그인

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified; // 이메일 인증 여부

    private String emailCheckToken; // 이메일 인증 시 필요한 토큰값

    private LocalDateTime emailCheckTokenGeneratedAt;

    // 프로필

    private LocalDateTime joinedAt;

    private String bio;

    private String ulr;

    private String occupation;

    private String location;

    // 기본적으로 db 에서 String 은 Varchar(255), 이미지가 커질수 있기때문에 애너테이션 적용
    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    // 알림 설정

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyEnrollmentResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    // Business Logic
    // 도메인 객체에 대한 비즈니스 로직이 있다고 하면 서비스 계층에 작성하는 것보다, 해당 도메인에 작성

    // 이메일 인증 토큰 수령
    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.setEmailVerified(true); // 인증여부 true
        this.setJoinedAt(LocalDateTime.now()); // 가입 날짜 적용
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
