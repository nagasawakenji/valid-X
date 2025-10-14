package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.mapper.PendingUserConverter;
import Nagasawa.valid_X.domain.model.*;
import Nagasawa.valid_X.application.service.VerificationService;
import Nagasawa.valid_X.application.service.VerifyService;
import Nagasawa.valid_X.domain.model.PendingUser;
import Nagasawa.valid_X.event.MagicLoginLinkRequestedEvent;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class VerifyServiceTest {

    @Mock
    private PendingUserMapper pendingUserMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private Clock clock;
    @Mock
    private VerificationService verificationService;
    @Mock
    private PendingUserConverter pendingUserConverter;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<UserEmail> userEmailCaptor;
    @Captor private ArgumentCaptor<Profile> profileCaptor;
    @Captor private ArgumentCaptor<Count> countCaptor;
    @Captor private ArgumentCaptor<UserPassword> userPasswordCaptor;

    private Clock fixedClock;
    private VerifyService verifyService;

    @BeforeEach
    void setup() {
        fixedClock = Clock.fixed(Instant.parse("2025-10-13T12:00:00Z"), ZoneOffset.UTC);
        verifyService = new VerifyService(
                pendingUserMapper,
                userMapper,
                clock,
                verificationService,
                pendingUserConverter,
                applicationEventPublisher
        );
    }

    @Test
    @DisplayName("正常系: 各モデルが正しく保存され、INSERT完了のイベントが発行される")
    void verify_createEachModels_andPublishesMagicLoginRequestEvent() {
        // 認証用のurlToken
        String urlToken = "token123";
        byte[] tokenHash = new byte[]{1, 2, 3};
        Instant now = Instant.now(fixedClock);
        Instant expiresAt = now.plus(Duration.ofMinutes(15));
        Instant checkTime = now.plus(Duration.ofMinutes(10));

        // テスト用PendingUser
        PendingUser saved = PendingUser.builder()
                .id(1L)
                .username("TestUser")
                .displayName("TestDisplay")
                .email("test@example.com")
                .passwordHash("hashed-pass")
                .tokenHash(tokenHash)
                .attemptCount(0)
                .lockedUntil(null)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .verified(false)
                .resendCount(0)
                .lastSentAt(now)
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .createdAt(null)
                .updatedAt(null)
                .build();

        when(verificationService.hashToken(urlToken)).thenReturn(tokenHash);
        when(pendingUserMapper.findPendingUserByTokenHash(tokenHash)).thenReturn(saved);
        when(clock.instant()).thenReturn(checkTime);

        // --- User ---
        User user = Nagasawa.valid_X.domain.model.User.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .displayName(saved.getDisplayName())
                .locale(saved.getLocale())
                .timezone(saved.getTimezone())
                .createdAt(saved.getCreatedAt())
                .build();

        // --- UserEmail ---
        UserEmail userEmail = Nagasawa.valid_X.domain.model.UserEmail.builder()
                .userId(saved.getId())
                .email(saved.getEmail())
                .createdAt(saved.getCreatedAt())
                .build();

        // --- Profile ---
        Profile profile = Nagasawa.valid_X.domain.model.Profile.builder()
                .userId(saved.getId())
                .bio(null) // ignore 指定のため
                .avatarUrl(null) // ignore 指定のため
                .protected_(false)
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();

        // --- Count ---
        Count count = Nagasawa.valid_X.domain.model.Count.builder()
                .userId(saved.getId())
                .followers(0)
                .following(0)
                .tweets(0)
                .updatedAt(saved.getUpdatedAt())
                .build();

        // --- UserPassword ---
        UserPassword userPassword = Nagasawa.valid_X.domain.model.UserPassword.builder()
                .userId(saved.getId())
                .passwordHash(saved.getPasswordHash())
                .algorithm("bcrypt")
                .strength(12)
                .passwordUpdatedAt(saved.getUpdatedAt())
                .rehashRequired(false)
                .build();

        when(pendingUserConverter.toUser(saved)).thenReturn(user);
        when(pendingUserConverter.toUserEmail(saved)).thenReturn(userEmail);
        when(pendingUserConverter.toUserPasswords(saved)).thenReturn(userPassword);
        when(pendingUserConverter.toCount(saved)).thenReturn(count);
        when(pendingUserConverter.toProfile(saved)).thenReturn(profile);

        // ただのINSERTなので、特に何もしない
        when(userMapper.insertUser(any())).thenReturn(1);
        when(userMapper.insertUserEmail(any())).thenReturn(1);
        when(userMapper.insertProfile(any())).thenReturn(1);
        when(userMapper.insertCount(any())).thenReturn(1);
        when(userMapper.insertUserPassword(any())).thenReturn(1);
        when(pendingUserMapper.deletePendingUserById(any())).thenReturn(1);

        // 実行
        verifyService.verify(urlToken);

        // メールイベント発行の検証
        // captorはverifyを実行後にしか機能しないことに注意する
        ArgumentCaptor<MagicLoginLinkRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(MagicLoginLinkRequestedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        // INSERTされたmodelの取得ハンドラ
        verify(userMapper).insertUser(userCaptor.capture());
        verify(userMapper).insertUserEmail(userEmailCaptor.capture());
        verify(userMapper).insertProfile(profileCaptor.capture());
        verify(userMapper).insertCount(countCaptor.capture());
        verify(userMapper).insertUserPassword(userPasswordCaptor.capture());

        // 各modelの検証
        assertThat(userCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(user);
        assertThat(userEmailCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(userEmail);
        assertThat(profileCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(profile);
        assertThat(countCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(count);
        assertThat(userPasswordCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(userPassword);

        // eventの検証
        // イベント内容の検証
        MagicLoginLinkRequestedEvent event = eventCaptor.getValue();
        assertThat(event.email()).isEqualTo("test@example.com");
    }

}
