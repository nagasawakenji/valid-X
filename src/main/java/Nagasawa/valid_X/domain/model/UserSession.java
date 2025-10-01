package Nagasawa.valid_X.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 端末ごとのログインセッションを表すモデル。
 * 「このデバイスは認可済み」＝ refresh_token の親単位。
 */
@Data
@Builder
public class UserSession {

    private UUID id;              // セッションID（UUID）
    private Long userId;          // 所属ユーザー
    private Integer sessionVersion; // セッションバージョン（JWT検証に利用）
    private Instant createdAt;    // 作成時刻
    private Instant lastSeenAt;   // 最後に使った時刻（任意更新）
    private Instant revokedAt;    // 無効化された時刻
    private Instant expiresAt;    // 任意の超長期失効時刻（null 可）
}