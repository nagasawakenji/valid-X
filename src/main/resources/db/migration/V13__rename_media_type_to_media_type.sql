ALTER TABLE media
  RENAME COLUMN type TO media_type;

-- CONSTRAINTで書いていないので、DROP...は必要ないのかも
-- 一応書いておく
ALTER TABLE media
  DROP CONSTRAINT IF EXISTS media_type_check,
  ADD CONSTRAINT media_media_type_check
    CHECK (media_type IN ('image', 'video', 'gif'));