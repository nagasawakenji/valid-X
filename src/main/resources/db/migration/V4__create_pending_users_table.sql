-- 仮登録に用いるテーブル
CREATE TABLE pending_users (
  id             BIGSERIAL PRIMARY KEY,
  username       VARCHAR(30) NOT NULL
                   CONSTRAINT chk_username_pattern_p
                     CHECK (username ~ '^(?!_)(?!.*__)[a-z0-9_]+(?<!_)$'),
  display_name   VARCHAR(50) NOT NULL
                   CONSTRAINT chk_display_name_len_p
                     CHECK (char_length(display_name) BETWEEN 1 AND 50),
  email          CITEXT NOT NULL UNIQUE,
  password_hash  TEXT NOT NULL,
  token_hash     BYTEA NOT NULL,
  expires_at     TIMESTAMPTZ NOT NULL,
  verified       BOOLEAN NOT NULL DEFAULT FALSE,
  resend_count   INTEGER NOT NULL DEFAULT 0,
  last_sent_at   TIMESTAMPTZ,
  locale         VARCHAR(10),
  timezone       VARCHAR(50),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- インデックス
CREATE INDEX idx_pending_users_expires ON pending_users(expires_at);
CREATE INDEX idx_pending_users_email   ON pending_users(email);
