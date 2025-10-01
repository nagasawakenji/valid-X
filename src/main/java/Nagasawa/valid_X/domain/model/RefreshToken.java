package Nagasawa.valid_X.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * refresh_tokens テーブルに対応。
 * 回転型（rotating）リフレッシュトークンの台帳。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /** PK: refresh_tokens.id（クライアントに提示されるID） */
    private UUID id;

    /** FK: refresh_tokens.user_id */
    private Long userId;

    /** FK: refresh_tokens.session_id (user_sessions.id) */
    private UUID sessionId;

    /** refresh_tokens.issued_at */
    private Instant issuedAt;

    /** refresh_tokens.expires_at */
    private Instant expiresAt;

    /** refresh_tokens.rotated_from（直前世代のID） */
    private UUID rotatedFrom;

    /** refresh_tokens.revoked_at（無効化された時刻） */
    private Instant revokedAt;

    /** 期限切れ判定 */
    public boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    /** 失効済み判定 */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** 失効マーキング（回転時・ログアウト時に使用） */
    public void revoke(Instant when) {
        this.revokedAt = when;
    }
}