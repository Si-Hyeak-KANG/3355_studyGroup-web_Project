package com.study3355.main;

import com.study3355.account.AccountRepository;
import com.study3355.account.AccountService;
import com.study3355.account.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Junit5 를 쓸 때는 @RunWith, @ExtendWith 를 쓸 필요가 없음. SpringBootTest 안에 있음
@SpringBootTest
@AutoConfigureMockMvc
public class MainControllerTest {

    // JUnit을 슬 때는 주입시 Autowired 사용해야함.
    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("james");
        signUpForm.setEmail("james@test.com");
        signUpForm.setPassword("12345678");

        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("이메일로 로그인하기")
    void login_with_email() throws Exception {

        //when
        mockMvc.perform(post("/login")
                        .param("username", "james@test.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("james"));

        //then
    }

    @Test
    @DisplayName("닉네임으로 로그인하기")
    void login_with_nickname() throws Exception {
        //when
        mockMvc.perform(post("/login")
                        .param("username", "james")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("james"));

        //then

    }

    @Test
    @DisplayName("로그인 실패")
    void login_fail() throws Exception {

        //when
        mockMvc.perform(post("/login")
                .param("username","11111")
                .param("password","00000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @WithMockUser
    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        //given

        //when
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
        //then

    }
}