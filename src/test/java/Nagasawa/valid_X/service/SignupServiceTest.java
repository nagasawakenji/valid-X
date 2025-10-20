package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.SignupService;
import Nagasawa.valid_X.application.service.VerificationService;
import Nagasawa.valid_X.domain.dto.RegisterForm;
import Nagasawa.valid_X.domain.dto.SignupResult;
import Nagasawa.valid_X.domain.dto.SignupStatus;
import Nagasawa.valid_X.domain.model.PendingUser;
import Nagasawa.valid_X.event.VerificationMailRequestedEvent;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class SignupServiceTest {

    @Mock
    private VerificationService verificationService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Clock clock;
    @Mock
    private PendingUserMapper pendingUserMapper;
    @Mock
    private UserMapper userMapper;

    @Captor
    private ArgumentCaptor<PendingUser> pendingUserCaptor;

    private SignupService signupService;

    private Clock fixedClock;

    @BeforeEach
    void setup() {
        fixedClock = Clock.fixed(Instant.parse("2025-10-13T12:00:00Z"), ZoneOffset.UTC);
        signupService = new SignupService(
                verificationService,
                applicationEventPublisher,
                passwordEncoder,
                fixedClock,
                pendingUserMapper,
                userMapper
                );
    }

    @Test
    @DisplayName("正常系: 新規サインアップでPendingUserが保存され、メールイベントが発行される")
    void signup_createPendingUser_andPublishesVerificationMailEvent() {

        // テスト用RegisterForm
        RegisterForm form = new RegisterForm(
                "TestUser",
                "TestDisplay",
                "test@example.com",
                "secret123",
                "ja",
                "Asia/Tokeyo"
        );

        String urlToken = "token123";
        byte[] tokenHash = new byte[]{1, 2, 3};

        when(pendingUserMapper.existsActiveByEmail("test@example.com")).thenReturn(false);
        when(userMapper.existsByUsername("TestUser")).thenReturn(false);
        when(verificationService.generateVerificationUrlToken()).thenReturn(urlToken);
        when(verificationService.hashToken(urlToken)).thenReturn(tokenHash);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-pass");

        // 実行
        SignupResult result = signupService.signup(form);

        // 返り値チェック
        assertThat(result.status()).isEqualTo(SignupStatus.ACCEPTED);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.resendCount()).isZero();
        assertThat(Duration.between(fixedClock.instant(), result.expiresAt()))
                .isEqualTo(Duration.ofMinutes(15));

        // PendingUserMapperの呼び出しの検証
        verify(pendingUserMapper).insertPendingUser(pendingUserCaptor.capture());
        // 保存された値の取得
        PendingUser saved = pendingUserCaptor.getValue();

        // PendingUserの検証
        assertThat(saved.getUsername()).isEqualTo("TestUser");
        assertThat(saved.getDisplayName()).isEqualTo("TestDisplay");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-pass");
        assertThat(saved.getTokenHash()).isEqualTo(tokenHash);
        assertThat(saved.getLastSentAt()).isEqualTo(fixedClock.instant());
        assertThat(saved.getExpiresAt()).isEqualTo(fixedClock.instant().plus(Duration.ofMinutes(15)));

        // メールイベント発行の検証
        ArgumentCaptor<VerificationMailRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(VerificationMailRequestedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        // イベント内容の検証
        VerificationMailRequestedEvent event = eventCaptor.getValue();
        assertThat(event.email()).isEqualTo("test@example.com");
        assertThat(event.urlToken()).isEqualTo("token123");

        // その他の呼び出しがないことを検証
        verifyNoMoreInteractions(pendingUserMapper, applicationEventPublisher, verificationService, passwordEncoder);

    }

    @Test
    @DisplayName("DB例外が発生した場合はロールバックされ、イベントは発行されない")
    void signup_rollsBack_whenInsertFails() {
        // Arrange
        RegisterForm form = new RegisterForm(
                "fail@example.com",
                "DisplayFail",
                "FailUser",
                "secret123",
                "ja",
                "Asia/Tokyo"
        );

        String urlToken = "tokenXYZ";
        byte[] tokenHash = new byte[]{1, 2, 3};

        when(verificationService.generateVerificationUrlToken()).thenReturn(urlToken);
        when(verificationService.hashToken(urlToken)).thenReturn(tokenHash);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        doThrow(new RuntimeException("DB insert failed"))
                .when(pendingUserMapper)
                .insertPendingUser(any());

        // Act & Assert
        assertThatThrownBy(() -> signupService.signup(form))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB insert failed");

        // insert が呼ばれたことは確認できる
        verify(pendingUserMapper).insertPendingUser(any());

        // 例外後なので、publishEvent は呼ばれていない
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("正常系: 既にPendingUserが存在する場合はDUPLICATEを返す")
    void signup_returnsDuplicate_whenPendingUserAlreadyExists() {
        // Arrange
        RegisterForm form = new RegisterForm(
                "NewUser",
                "NewDisplay",
                "existing@example.com",
                "password123",
                "en",
                "UTC"
        );

        when(pendingUserMapper.existsActiveByEmail("existing@example.com")).thenReturn(true);

        // 実行
        SignupResult result = signupService.signup(form);

        // 検証
        assertThat(result.status()).isEqualTo(SignupStatus.DUPLICATE);
        assertThat(result.email()).isEqualTo("existing@example.com");

        // insertPendingUser は呼ばれていない
        verify(pendingUserMapper, never()).insertPendingUser(any());

        // イベントも発行されていない
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("正常系: 既にユーザー名が存在する場合はDUPLICATEを返す")
    void signup_returnsDuplicate_whenUsernameAlreadyExists() {
        // Arrange
        RegisterForm form = new RegisterForm(
                "existingUser",
                "DisplayName",
                "newuser@example.com",
                "password123",
                "en",
                "UTC"
        );

        when(pendingUserMapper.existsActiveByEmail("newuser@example.com")).thenReturn(false);
        when(userMapper.existsByUsername("existingUser")).thenReturn(true);

        // 実行
        SignupResult result = signupService.signup(form);

        // 検証
        assertThat(result.status()).isEqualTo(SignupStatus.DUPLICATE);
        assertThat(result.email()).isEqualTo("newuser@example.com");

        // insertPendingUser は呼ばれていない
        verify(pendingUserMapper, never()).insertPendingUser(any());

        // イベントも発行されていない
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
