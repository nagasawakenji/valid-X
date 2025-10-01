-- user_sessions:端末ごとのログイン情報を保存
CREATE TABLE user_sessions(
  id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  session_version    INTEGER NOT NULL DEFAULT 1,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_seen_at       TIMESTAMPTZ,
  revoked_at         TIMESTAMPTZ,
  expires_at         TIMESTAMPTZ
);

CREATE INDEX idx_user_sessions_user_active
  ON user_sessions (user_id)
  WHERE revoked_at IS NULL;

CREATE INDEX idx_user_sessions_user_last_seen
  ON user_sessions (user_id, last_seen_at DESC);