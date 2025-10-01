-- refresh_tokens：回転型リフレッシュトークン（セッションごとに複数世代）
CREATE TABLE refresh_tokens (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(), -- clientに渡すID(opaque/JWT)
  user_id          bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  session_id       uuid   NOT NULL REFERENCES user_sessions(id) ON DELETE CASCADE,

  issued_at        timestamptz NOT NULL DEFAULT now(),
  expires_at       timestamptz NOT NULL,
  rotated_from     uuid,             -- 直前の世代ID（追跡）
  revoked_at       timestamptz
);

CREATE INDEX idx_refresh_active
  ON refresh_tokens (session_id, expires_at)
  WHERE revoked_at IS NULL;