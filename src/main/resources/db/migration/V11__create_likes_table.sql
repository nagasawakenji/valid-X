CREATE TABLE likes (
  user_id   BIGINT NOT NULL REFERENCES users(id),
  tweet_id  BIGINT NOT NULL REFERENCES tweets(tweet_id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, tweet_id)
);