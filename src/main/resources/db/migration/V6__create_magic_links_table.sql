-- magic_links：使い切り・短命。DBには生トークンを保存しない（HMAC or SHA-256）
CREATE TABLE magic_links (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  token_hash       BYTEA  NOT NULL,           -- HMAC-SHA256(token_raw)
  hmac_key_id      SMALLINT NOT NULL DEFAULT 1,
  expires_at       TIMESTAMPTZ NOT NULL,
  used_at          TIMESTAMPTZ,

  UNIQUE (hmac_key_id, token_hash)
);

CREATE INDEX idx_magic_links_user_active
  ON magic_links (user_id, expires_at)
  WHERE used_at IS NULL;

CREATE INDEX idx_magic_links_expiration
  ON magic_links (expires_at);