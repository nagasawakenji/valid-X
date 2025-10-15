package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.LogoutService;
import Nagasawa.valid_X.domain.dto.LogoutResult;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserSessionMapper userSessionMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private Clock fixedClock;

    private LogoutService logoutService;

    private final Instant FIXED_NOW = Instant.parse("2025-10-14T12:00:00Z");
    private final UUID SESSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @BeforeEach
    void setup() {
        when(fixedClock.instant()).thenReturn(FIXED_NOW);
        logoutService = new LogoutService(userMapper, userSessionMapper, refreshTokenMapper, fixedClock);
    }

    @Test
    @DisplayName("正常系: セッションとユーザーが存在し、未revokedの場合、revokeされLogoutResultが返る")
    void logout_success_notRevokedYet() {
        // Given
        Long userId = 5L;
        User user = User.builder().id(userId).build();
        UserSession session = UserSession.builder()
                .id(SESSION_ID)
                .userId(userId)
                .revokedAt(null)
                .build();

        when(userMapper.findById(userId)).thenReturn(user);
        when(userSessionMapper.findById(SESSION_ID)).thenReturn(session);
        when(userSessionMapper.revoke(SESSION_ID, FIXED_NOW)).thenReturn(1);
        when(refreshTokenMapper.revokeBySession(SESSION_ID, FIXED_NOW)).thenReturn(3);

        // When
        LogoutResult result = logoutService.logout(userId, SESSION_ID);

        // Then
        verify(userSessionMapper).revoke(SESSION_ID, FIXED_NOW);
        verify(refreshTokenMapper).revokeBySession(SESSION_ID, FIXED_NOW);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSessionUuid()).isEqualTo(SESSION_ID);
        assertThat(result.isAlreadyRevoked()).isFalse();
        assertThat(result.getRevokedAt()).isEqualTo(FIXED_NOW);
        assertThat(result.getRefreshRevoked()).isEqualTo(3);
    }

    @Test
    @DisplayName("異常系: userが存在しない場合はNotFoundProblemException(user_not_found)が発生する")
    void logout_userNotFound_throwsException() {
        // Given
        when(userMapper.findById(5L)).thenReturn(null);

        // Then
        assertThatThrownBy(() -> logoutService.logout(5L, SESSION_ID))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("user_not_found");

        verify(userMapper).findById(5L);
        verifyNoMoreInteractions(userSessionMapper, refreshTokenMapper);
    }

    @Test
    @DisplayName("異常系: sessionが存在しない、または他人のsessionの場合NotFoundProblemException(session_not_found)")
    void logout_sessionNotFoundOrOtherUser_throwsException() {
        // Given
        Long userId = 5L;
        User user = User.builder().id(userId).build();

        when(userMapper.findById(userId)).thenReturn(user);
        // セッションが他人のもの
        UserSession session = UserSession.builder()
                .id(SESSION_ID)
                .userId(999L)
                .build();
        when(userSessionMapper.findById(SESSION_ID)).thenReturn(session);

        // Then
        assertThatThrownBy(() -> logoutService.logout(userId, SESSION_ID))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("session_not_found");

        verify(userSessionMapper, never()).revoke(any(), any());
        verify(refreshTokenMapper, never()).revokeBySession(any(), any());
    }

    // ------------------------------------------------------
    // ✅ すでにrevoked済み
    // ------------------------------------------------------
    @Test
    @DisplayName("正常系: すでにrevoked済みの場合、再revokeせず既存revokedAtをそのまま返す")
    void logout_alreadyRevoked() {
        // Given
        Long userId = 5L;
        Instant oldRevokedAt = FIXED_NOW.minusSeconds(3600);

        User user = User.builder().id(userId).build();
        UserSession session = UserSession.builder()
                .id(SESSION_ID)
                .userId(userId)
                .revokedAt(oldRevokedAt)
                .build();

        when(userMapper.findById(userId)).thenReturn(user);
        when(userSessionMapper.findById(SESSION_ID)).thenReturn(session);
        when(refreshTokenMapper.revokeBySession(SESSION_ID, FIXED_NOW)).thenReturn(2);

        // When
        LogoutResult result = logoutService.logout(userId, SESSION_ID);

        // Then
        verify(userSessionMapper, never()).revoke(any(), any());
        verify(refreshTokenMapper).revokeBySession(SESSION_ID, FIXED_NOW);

        assertThat(result.isAlreadyRevoked()).isTrue();
        assertThat(result.getRevokedAt()).isEqualTo(oldRevokedAt);
        assertThat(result.getRefreshRevoked()).isEqualTo(2);
    }
}