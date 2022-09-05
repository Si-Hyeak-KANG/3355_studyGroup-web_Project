package com.study3355.config;

import com.study3355.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccountService accountService;
    private final DataSource dataSource;

    // 기본적으론 CSRF(Cross-Site Request Forgery)가 활성화되어있음
    // 타사이트에서 form 요청을 보내는 것을 방어
    // Thymeleaf 템플릿으로 만들경우, CSRF 토큰 기능을 지원
    // 따라서 토큰없이 데이터가 들어온다면, 403 에러 발생
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login").permitAll();

        http.logout()
                .logoutSuccessUrl("/");
        // hashing 기반 토큰 방식 -> 위험
        //http.rememberMe().key("qwert");

        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository()); // username, 토큰(랜덤), 시리즈(랜덤, 고정)

        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        // JdbcTokenRepositoryImpl의 메서드를 사용하기 위해선 구현체에서 명시한 테이블이 있어야함.
        return jdbcTokenRepository;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() throws Exception {
        // static resource security filter 를 적용하지마라
        // static 에서 흔히 사용되는 위치
        return web -> web.ignoring()
                .mvcMatchers("/node_modules/**","/error")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

}