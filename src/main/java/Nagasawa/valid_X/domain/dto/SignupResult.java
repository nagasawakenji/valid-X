package Nagasawa.valid_X.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record SignupResult(
        SignupStatus status,
        String email,
        Instant expiresAt,      // 検証リンクの有効期限
        Integer resendCount     // 現在の送信回数（任意）
) {}