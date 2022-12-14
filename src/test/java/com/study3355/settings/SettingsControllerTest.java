package com.study3355.settings;

import com.study3355.WithAccount;
import com.study3355.account.AccountRepository;
import com.study3355.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정하기 - 입력값 정상")
    //@WithUserDetails(value = "james",setupBefore = TestExecutionEvent.TEST_EXECUTION) // beforeEach보다 먼저 실행됨.,심지어 설정을해도 before 보다 먼저 실행
    @WithAccount("james")
    void updateProfile() throws Exception {
        //given
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));
        //when
        Account james = accountRepository.findByNickname("james");

        //then
        assertEquals(bio,james.getBio());
    }

    @Test
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @WithAccount("james")
    void updateProfile_error() throws Exception {
        //given
        String bio = "짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우."; // 35자가 넘는 경우
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());
        //when
        Account james = accountRepository.findByNickname("james");

        //then
        assertNull(james.getBio());
    }

    @Test
    @DisplayName("프로필 수정 폼")
    //@WithUserDetails(value = "james",setupBefore = TestExecutionEvent.TEST_EXECUTION) // beforeEach보다 먼저 실행됨.,심지어 설정을해도 before 보다 먼저 실행
    @WithAccount("james")
    void updateProfileForm() throws Exception {
        //given
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("james")
    @Test
    @DisplayName("패스워드 수정 폼")
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("james")
    @Test
    @DisplayName("패스워드 수정 - 입력값 정상")
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm","123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account james = accountRepository.findByNickname("james");
        assertTrue(passwordEncoder.matches("123456789", james.getPassword()));
    }

    @WithAccount("james")
    @Test
    @DisplayName("패스워드 수정 - 입력값 에러 - new 패스워드 불일치")
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }
}