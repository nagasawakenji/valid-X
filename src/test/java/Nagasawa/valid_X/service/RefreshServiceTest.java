package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.JwtService;
import Nagasawa.valid_X.application.service.RefreshService;
import Nagasawa.valid_X.domain.dto.RefreshResult;
import Nagasawa.valid_X.domain.model.RefreshToken;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.exception.invalidProblems.InvalidRefreshTokenProblemException;
import Nagasawa.valid_X.exception.invalidProblems.InvalidSessionProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.MagicLinkMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private MagicLinkMapper magicLinkMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private UserSessionMapper userSessionMapper;
    @Mock
    private JwtService jwtService;

    private Clock fixedClock;

    @InjectMocks
    private RefreshService refreshService;

    private final UUID refreshId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final Long userId = 5L;

    private Instant now;

    @BeforeEach
    void setup() {
        now = Instant.parse("2025-10-14T12:00:00Z");
        fixedClock = Clock.fixed(now, ZoneOffset.UTC);
        refreshService = new RefreshService(
                magicLinkMapper,
                userMapper,
                refreshTokenMapper,
                userSessionMapper,
                fixedClock,
                jwtService
        );

        // privateフィールドをリフレクションで設定
        setPrivateField(refreshService, "accessTtlMinutes", 15L);
        setPrivateField(refreshService, "refreshTtlDays", 30L);
        setPrivateField(refreshService, "refreshCookieName", "refresh_token");
        setPrivateField(refreshService, "refreshCookiePath", "/v1/auth");
    }

    /** リフレクションで private フィールドを書き換える */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = RefreshService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("正常系: 有効なRefreshTokenで新しいJWTとCookieを発行")
    void refresh_success() {
        RefreshToken oldToken = RefreshToken.builder()
                .id(refreshId)
                .userId(userId)
                .sessionId(sessionId)
                .issuedAt(now.minusSeconds(100))
                .expiresAt(now.plus(Duration.ofDays(10)))
                .build();

        UserSession session = UserSession.builder()
                .id(sessionId)
                .sessionVersion(2)
                .expiresAt(now.plus(Duration.ofDays(10)))
                .build();

        User user = User.builder()
                .id(userId)
                .username("TestUser")
                .build();

        when(refreshTokenMapper.findActiveById(refreshId)).thenReturn(oldToken);
        when(userSessionMapper.findById(sessionId)).thenReturn(session);
        when(userMapper.findById(userId)).thenReturn(user);
        when(jwtService.issueAccessToken(eq(userId), eq("kenji"), eq(sessionId), eq(2), isNull()))
                .thenReturn("mock-jwt");

        RefreshResult result = refreshService.refresh(refreshId);

        // 結果検証
        assertThat(result.jwt()).isEqualTo("mock-jwt");
        assertThat(result.accessTtlSecond()).isEqualTo(15L * 60);
        ResponseCookie cookie = result.cookie();
        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.getPath()).isEqualTo("/v1/auth");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getMaxAge().toDays()).isEqualTo(30);

        verify(refreshTokenMapper).revoke(eq(refreshId), eq(now));
        verify(refreshTokenMapper).insert(any(RefreshToken.class));
    }

    @Test
    @DisplayName("異常系: 無効なRefreshTokenで例外が発生")
    void refresh_invalidRefreshToken_throwsException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .id(refreshId)
                .userId(userId)
                .sessionId(sessionId)
                .issuedAt(now.minus(Duration.ofDays(31)))
                .expiresAt(now.minusSeconds(1)) // 期限切れ
                .build();

        when(refreshTokenMapper.findActiveById(refreshId)).thenReturn(expiredToken);

        assertThatThrownBy(() -> refreshService.refresh(refreshId))
                .isInstanceOf(InvalidRefreshTokenProblemException.class);

        verify(refreshTokenMapper, never()).revoke(any(), any());
    }

    @Test
    @DisplayName("異常系: セッションが無効な場合に例外発生")
    void refresh_invalidSession_throwsException() {
        RefreshToken validToken = RefreshToken.builder()
                .id(refreshId)
                .userId(userId)
                .sessionId(sessionId)
                .issuedAt(now.minusSeconds(100))
                .expiresAt(now.plus(Duration.ofDays(10)))
                .build();

        UserSession expiredSession = UserSession.builder()
                .id(sessionId)
                .sessionVersion(1)
                .expiresAt(now.minus(Duration.ofDays(1))) // 期限切れ
                .build();

        when(refreshTokenMapper.findActiveById(refreshId)).thenReturn(validToken);
        when(userSessionMapper.findById(sessionId)).thenReturn(expiredSession);

        assertThatThrownBy(() -> refreshService.refresh(refreshId))
                .isInstanceOf(InvalidSessionProblemException.class);

        verify(refreshTokenMapper, never()).revoke(any(), any());
    }
}