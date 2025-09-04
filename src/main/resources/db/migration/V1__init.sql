-- 拡張（メールの大文字小文字を無視したユニークに使う）
CREATE EXTENSION IF NOT EXISTS citext;

-- ユーザー（公開プロフィールの最小集合）
CREATE TABLE users (
  id            BIGSERIAL PRIMARY KEY,
  username      VARCHAR(30) NOT NULL UNIQUE
                 CONSTRAINT chk_username_pattern
                   CHECK (username ~ '^(?!_)(?!.*__)[a-z0-9_]+(?<!_)$'),
  display_name  VARCHAR(50) NOT NULL
                 CONSTRAINT chk_display_name_len
                   CHECK (char_length(display_name) BETWEEN 1 AND 50),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- メール（1ユーザー=1メール想定。将来拡張するなら is_primary などを追加）
CREATE TABLE user_emails (
  user_id      BIGINT  PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  email        CITEXT  NOT NULL UNIQUE,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_user_emails_user ON user_emails(user_id);

-- プロフィール（1ユーザー=1プロフィール）
CREATE TABLE profiles (
  user_id     BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  bio         VARCHAR(160),
  avatar_url  TEXT,
  protected   BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ
);

-- 集計カウント (キャッシュするので1ユーザーにつき1行)
CREATE TABLE counts (
  user_id     BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  followers   INTEGER NOT NULL DEFAULT 0,
  following   INTEGER NOT NULL DEFAULT 0,
  tweets      INTEGER NOT NULL DEFAULT 0,
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ツイート
CREATE TABLE tweets (
  tweet_id      BIGSERIAL PRIMARY KEY,               -- グローバル一意ID
  user_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content       VARCHAR(280) NOT NULL,
  in_reply_to_tweet_id BIGINT REFERENCES tweets(tweet_id) ON DELETE SET NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 添付メディアのマスタ
CREATE TABLE media (
  media_id     BIGSERIAL PRIMARY KEY,
  type         VARCHAR(16) NOT NULL CHECK (type IN ('image','video','gif')),
  mime_type    VARCHAR(100) NOT NULL,
  bytes        BIGINT NOT NULL CHECK (bytes > 0),
  width        INT,                  -- 画像/動画のピクセル
  height       INT,
  duration_ms  INT,                  -- 動画/GIFのみ
  sha256       BYTEA,                -- 重複検出・ウイルススキャン済み印
  blurhash     VARCHAR(100),         -- 画像プレビュー用（任意）
  storage_key  TEXT NOT NULL,        -- S3等のキー
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ツイートとメディアの関連（順序付き多対1）
CREATE TABLE tweet_media (
  tweet_id     BIGINT NOT NULL REFERENCES tweets(tweet_id) ON DELETE CASCADE,
  media_id     BIGINT NOT NULL REFERENCES media(media_id) ON DELETE RESTRICT,
  position     INT    NOT NULL CHECK (position BETWEEN 0 AND 9),
  PRIMARY KEY (tweet_id, position)
);

-- インデックス
-- よく使用する操作に対して作成

-- 各ユーザーに対して新しい順でツイート一覧を取得する際に使う
CREATE INDEX idx_tweets_user_created_at ON tweets(user_id, created_at DESC);

-- ツイート全体について新しい順
CREATE INDEX idx_tweets_created_at_desc ON tweets(created_at DESC);

-- ツイートからメディアを参照する
CREATE INDEX idx_tweet_media_tweet ON tweet_media(tweet_id);
-- メディアからツイートを参照する
CREATE INDEX idx_tweet_media_media ON tweet_media(media_id);





