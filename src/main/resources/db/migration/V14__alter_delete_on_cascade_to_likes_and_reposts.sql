-- fkeyの解除
ALTER TABLE likes
  DROP CONSTRAINT IF EXISTS likes_tweet_id_fkey;

-- ON DELETE CASCADEで制約を再作成
ALTER TABLE likes
  ADD CONSTRAINT likes_tweet_id_fkey
  FOREIGN KEY (tweet_id) REFERENCES tweets(tweet_id)
  ON DELETE CASCADE;

-- repostsにも同様の処理を行う
ALTER TABLE reposts
  DROP CONSTRAINT IF EXISTS reposts_tweet_id_fkey;
ALTER TABLE reposts
  ADD CONSTRAINT reposts_tweet_id_fkey
  FOREIGN KEY (tweet_id) REFERENCES tweets(tweet_id)
  ON DELETE CASCADE;