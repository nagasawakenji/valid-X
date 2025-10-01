// SignupService.java
package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.RegisterForm;
import Nagasawa.valid_X.domain.dto.SignupResult;
import Nagasawa.valid_X.domain.dto.SignupStatus;
import Nagasawa.valid_X.event.VerificationMailRequestedEvent;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
import Nagasawa.valid_X.domain.model.PendingUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
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
        // if (pendingUserMapper.existsActiveByEmail(normalizedEmail)) {
        //     return new SignupResult(SignupStatus.DUPLICATE, normalizedEmail, null, null, null);
        // }

        String urlToken = verificationService.generateVerificationUrlToken();
        byte[] tokenHash = verificationService.hashToken(urlToken);

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