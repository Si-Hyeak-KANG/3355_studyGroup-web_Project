package com.study3355.account;

import com.study3355.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    // 인증 메일 확인 테스트

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    public void checkEmailToken_with_wrong_input() throws Exception {
        //given
        mockMvc.perform(
                get("/check-email-token")
                        .param("token","qwerqwer")
                        .param("email","test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());// 스프링 시큐리티가 적용된 mockMvc일 때 사용가능: 인증이 안되어 있음, csrf() 기능처럼
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 정상")
    void checkEmailToken_correct_input() throws Exception {

        //given
        Account account = Account.builder()
                .email("test@test.com")
                .password("12345678")
                .nickname("test")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        //when
        mockMvc.perform(
                get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail())
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("test"));// 스프링 시큐리티가 적용된 mockMvc일 때 사용가능: 인증되어 있음, csrf() 기능처럼
    }

    // 회원 가입 처리 테스트

    @Test
    @DisplayName("회원 가입 화면 보이는지 테스트")
    void signUpForm() throws Exception {

        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 오류")
    public void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname","james")
                .param("email","errorEmail..")
                .param("password","12345")
                .with(csrf())) // csrf로 인한 403 에러 방지
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상")
    public void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname","james")
                        .param("email","test@test.com")
                        .param("password","12345678")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("james")); // 가입한 회원으로 인증이 되었는지도 체크 가능

        Account account = accountRepository.findByEmail("test@test.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(),"12345678"); // 패스워드 인코딩 체크
        assertNotNull(account.getEmailCheckToken()); // 토큰이 있는지 체크

        assertTrue(accountRepository.existsByEmail("test@test.com"));

        // send 가 호출이 되는가 체크
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

}