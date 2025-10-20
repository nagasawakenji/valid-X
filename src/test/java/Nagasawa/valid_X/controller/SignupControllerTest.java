package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.domain.dto.RegisterForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("正常系: ユーザー登録が成功する")
    void signup_success() throws Exception {
        RegisterForm form = RegisterForm.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .email("test@example.com")
                .password("testpass123")
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .build();

        // 実行
        mockMvc.perform(post("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("異常系: 重複登録で409を返す")
    void signup_exception409_duplicated() throws Exception {
        RegisterForm form = RegisterForm.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .email("test@example.com")
                .password("testpass123")
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .build();

        // 一度めの実行は成功する
        mockMvc.perform(post("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 二度目の実行は重複で409を返す
        mockMvc.perform(post("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("DUPLICATE"));

    }
}
