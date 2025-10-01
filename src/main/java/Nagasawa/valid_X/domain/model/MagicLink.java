package Nagasawa.valid_X.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * magic_links テーブルに対応。
 * 使い切り・短TTLのワンタイムトークン（ハッシュのみ保持）。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicLink {

    /** PK: magic_links.id */
    private UUID id;

    /** FK: magic_links.user_id */
    private Long userId;

    /** magic_links.token_hash (HMAC-SHA256(raw)) */
    private byte[] tokenHash;

    /** magic_links.hmac_key_id (鍵世代) */
    private short hmacKeyId;

    /** magic_links.expires_at */
    private Instant expiresAt;

    /** magic_links.used_at (null なら未使用) */
    private Instant usedAt;

    /** 期限切れ判定 */
    public boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    /** 使用済み判定（ワンタイム） */
    public boolean isUsed() {
        return usedAt != null;
    }

    /** 消費（使用済みにマーキング） */
    public void markUsed(Instant when) {
        this.usedAt = when;
    }
}