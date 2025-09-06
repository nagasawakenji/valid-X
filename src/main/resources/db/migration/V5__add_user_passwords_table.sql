-- 認証専用の秘匿情報は users から分離
CREATE TABLE user_passwords (
  user_id              BIGINT PRIMARY KEY
                         REFERENCES users(id) ON DELETE CASCADE,
  password_hash        TEXT    NOT NULL,             -- bcrypt/argon2 のハッシュ文字列
  algorithm            VARCHAR(20) NOT NULL DEFAULT 'bcrypt',  -- 'bcrypt' / 'argon2id' 等
  strength             INTEGER,                      -- 例: bcrypt の cost（10〜14推奨）
  password_updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  rehash_required      BOOLEAN NOT NULL DEFAULT FALSE
);