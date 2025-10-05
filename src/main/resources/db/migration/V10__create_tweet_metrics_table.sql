CREATE TABLE tweet_metrics (
  tweet_id     BIGINT PRIMARY KEY REFERENCES tweets(tweet_id) ON DELETE CASCADE,
  like_count   INTEGER NOT NULL DEFAULT 0,
  repost_count INTEGER NOT NULL DEFAULT 0,
  reply_count  INTEGER NOT NULL DEFAULT 0
);