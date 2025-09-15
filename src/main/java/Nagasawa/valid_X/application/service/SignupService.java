package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.RegisterForm;
import Nagasawa.valid_X.event.VerificationMailRequestedEvent;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
import Nagasawa.valid_X.domain.model.PendingUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SignupService {
    // サインアップに関するロジックをまとめている
    private final VerificationService verificationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final PendingUserMapper pendingUserMapper;

    @Transactional
    public ResponseEntity<Void> signup(RegisterForm registerForm) {
        Instant now = Instant.now(clock);
        String normalizedEmail = registerForm.getEmail().trim().toLowerCase();

        // URLトークンの生成
        String urlToken = verificationService.generateVerificationUrlToken();
        String tokenHash = verificationService.hashToken(urlToken);

        // pendingUserを作成する
        PendingUser pendingUser = PendingUser.builder()
                .username(registerForm.getUsername())
                .displayName(registerForm.getDisplayName())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(registerForm.getPassword()))
                .tokenHash(tokenHash)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .resendCount(0)
                .lastSentAt(now)
                .locale(registerForm.getLocale())
                .timezone(registerForm.getTimezone())
                .build();

        // pending_usersへのinsert
        pendingUserMapper.insertPendingUser(pendingUser);

        // insert完了のイベント通知
        applicationEventPublisher.publishEvent(new VerificationMailRequestedEvent(normalizedEmail, urlToken));

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
