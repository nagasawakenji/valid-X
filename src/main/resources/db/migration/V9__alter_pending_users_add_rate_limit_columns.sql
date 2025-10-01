ALTER TABLE pending_users
  ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN locked_until  TIMESTAMPTZ;

-- 2つのカラムが抜けていたので追加