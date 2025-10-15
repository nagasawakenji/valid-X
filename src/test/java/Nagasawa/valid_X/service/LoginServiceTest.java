package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.JwtService;
import Nagasawa.valid_X.application.service.LoginService;
import Nagasawa.valid_X.domain.dto.AccessIssueResult;
import Nagasawa.valid_X.domain.model.RefreshToken;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private UserSessionMapper sessionMapper;
    @Mock
    private RefreshTokenMapper refreshMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private Clock fixedClock;

    @Captor
    private ArgumentCaptor<UserSession> sessionArgumentCaptor;
    @Captor
    private ArgumentCaptor<RefreshToken> refreshArgumentCaptor;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private LoginService loginService;

    // フィールドに値を設定するメソッド
    /** リフレクションで private フィールドを書き換える */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setup() {
        loginService = new LoginService(
                sessionMapper,
                refreshMapper,
                userMapper,
                jwtService,
                fixedClock
        );

        // privateフィールドを定義していく
        setPrivateField(loginService, "accessTtlMinutes", 15L);
        setPrivateField(loginService, "refreshTtlDays", 30L);
        setPrivateField(loginService, "refreshCookieName", "refresh_token");
        setPrivateField(loginService, "refreshCookiePath", "/v1/auth");
        // LoginServiceでも使ってないが、一応設定はしておく
        setPrivateField(loginService, "refreshCookieDomain", null);
    }

    @Test
    @DisplayName("正常系: 各モデルが正しく保存され、AccessIssueResultが正しい値で返却される")
    void issueForUser_correctEachModels_andCorrectAccessIssueResult() {

        Instant now = Instant.parse("2025-10-13T12:00:00Z");
        Instant expiresAt = now.plus(Duration.ofDays(30));

        String access = "mock-token-value";

        UUID uuid = UUID.randomUUID();

        User user = User.builder()
                .id(5L)
                .username("TestUser")
                .displayName("TestDisplay")
                .createdAt(now)
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .build();

        UserSession session = UserSession.builder()
                .id(uuid)
                .userId(5L)
                .sessionVersion(1)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        RefreshToken rt = RefreshToken.builder()
                .id(uuid)
                .userId(5L)
                .sessionId(session.getId())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        when(fixedClock.instant()).thenReturn(now);
        when(userMapper.findById(5L)).thenReturn(user);
        when(jwtService.issueAccessToken(
                5L,
                user.getUsername(),
                session.getId(),
                session.getSessionVersion(),
                List.of("USER")
        )).thenReturn(access);
        // uuidは本来INSERTによりセットされるので、テストでは自前でセットする
        doAnswer(inv -> {
            RefreshToken arg = inv.getArgument(0);
            arg.setId(uuid);  // ← ID をテスト側で強制設定
            return null; // void メソッドなので return は不要
        }).when(refreshMapper).insert(any(RefreshToken.class));
        doAnswer(inv -> {
            UserSession arg = inv.getArgument(0);
            arg.setId(uuid);
            return null;
        }).when(sessionMapper).insert(any(UserSession.class));

        // 実行
        AccessIssueResult result = loginService.issueForUser(5L);

        // resultの検証
        assertThat(result.accessToken()).isEqualTo(access);
        assertThat(result.accessTtlSeconds()).isEqualTo(900L);
        assertThat(result.responseCookie().isHttpOnly()).isTrue();
        assertThat(result.responseCookie().isSecure()).isTrue();
        assertThat(result.responseCookie().getSameSite()).isEqualTo("None");
        assertThat(result.responseCookie().getPath()).isEqualTo("/v1/auth");
        assertThat(result.responseCookie().getMaxAge()).isEqualTo(Duration.ofDays(30));

        // 各モデルの取得
        verify(sessionMapper).insert(sessionArgumentCaptor.capture());
        verify(refreshMapper).insert(refreshArgumentCaptor.capture());

        // 各モデルの値の検証
        assertThat(sessionArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(session);
        assertThat(refreshArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(rt);
    }
}
