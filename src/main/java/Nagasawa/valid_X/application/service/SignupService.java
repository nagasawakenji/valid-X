// SignupService.java
package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.RegisterForm;
import Nagasawa.valid_X.domain.dto.SignupResult;
import Nagasawa.valid_X.domain.dto.SignupStatus;
import Nagasawa.valid_X.event.VerificationMailRequestedEvent;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
import Nagasawa.valid_X.domain.model.PendingUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {
    private final VerificationService verificationService;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final PendingUserMapper pendingUserMapper;

    @Transactional
    public SignupResult signup(RegisterForm form) {
        Instant now = Instant.now(clock);
        String normalizedEmail = form.getEmail().trim().toLowerCase();

        // 既存チェックなど（必要なら）
        // 現在は省いています (大量リクエストを想定する場合は有効化する予定)
        // if (pendingUserMapper.existsActiveByEmail(normalizedEmail)) {
        //     return new SignupResult(SignupStatus.DUPLICATE, normalizedEmail, null, null, null);
        // }

        String urlToken = verificationService.generateVerificationUrlToken();
        byte[] tokenHash = verificationService.hashToken(urlToken);

        // 検証用
        // 後で必ず消す!!
        log.info("[DEV] url token (base64url) = {}", urlToken);

        PendingUser pending = PendingUser.builder()
                .username(form.getUsername())
                .displayName(form.getDisplayName())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .tokenHash(tokenHash)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .resendCount(0)
                .lastSentAt(now)
                .locale(form.getLocale())
                .timezone(form.getTimezone())
                .build();

        pendingUserMapper.insertPendingUser(pending); // pending.id を採番

        // メール送信はイベントへ（AFTER_COMMIT でリスナが送信）
        publisher.publishEvent(new VerificationMailRequestedEvent(normalizedEmail, urlToken));

        return new SignupResult(
                SignupStatus.ACCEPTED,
                normalizedEmail,
                pending.getExpiresAt(),
                pending.getResendCount()
        );
    }
}