package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.validation.TweetValidator;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.ReplyMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyService {

    private final ReplyMapper replyMapper;
    private final PostMapper postMapper;
    private final TweetValidator tweetValidator;
    private final TweetConverter tweetConverter;
    private final TweetMetricsMapper tweetMetricsMapper;
    private final LocalMediaStorageService localMediaStorageService;

    @Transactional
    public PostResult reply(Long parentTweetId, Long userId, PostForm postForm) {

        if (!replyMapper.parentExists(parentTweetId)) {
            throw new IllegalArgumentException("parent tweet not found: id=" + parentTweetId);
        }

        Tweet tweet = tweetConverter.toTweet(postForm, userId);
        // postFormのinReplyToTweetを上書きする
        tweet.setInReplyToTweetId(parentTweetId);

        tweetValidator.validateContent(tweet.getContent());

        // ツイート INSERT
        postMapper.insertTweet(tweet);
        Long tweetId = tweet.getTweetId();

        // メトリクス初期化
        tweetMetricsMapper.insertInit(tweetId);

        // レスポンス用
        List<Media> medias = new ArrayList<>();

        // メディア処理（PostService と同等のロジック）
        if (postForm.medias() != null && !postForm.medias().isEmpty()) {
            for (int i = 0; i < postForm.medias().size(); i++) {
                var m = postForm.medias().get(i);

                // dataUrl ガード
                if (m.dataUrl() == null || m.dataUrl().isBlank()) {
                    log.warn("media[{}]: dataUrl is null/blank. Skip.", i);
                    continue;
                }

                // bytes へ一度だけデコード
                byte[] bytes;
                try {
                    bytes = decodeDataUrlBytes(m.dataUrl());
                } catch (RuntimeException e) {
                    log.warn("media[{}]: invalid dataUrl. Skip.", i, e);
                    continue;
                }
                Long size = (long) bytes.length;

                // MIME と拡張子
                String mimeType = m.mimeType();          // 例: image/jpeg（null の可能性あり）
                String ext = guessExt(mimeType);         // 例: .jpg
                String targetFileName = "tweet_" + tweetId + "_" + i + ext;

                // 永続化（ローカルディスク）
                String storageKey;
                try {
                    storageKey = localMediaStorageService.saveBytes(bytes, targetFileName);
                } catch (RuntimeException e) {
                    log.warn("media[{}]: persist failed. Skip linking.", i, e);
                    continue;
                }
                if (storageKey == null || storageKey.isBlank()) {
                    log.warn("media[{}]: storageKey empty. Skip linking.", i);
                    continue;
                }

                // Media モデルを組み立て
                Media media = Media.builder()
                        .mediaType(inferMediaType(mimeType))
                        .mimeType(mimeType)
                        .bytes(size)
                        .width(m.width())
                        .height(m.height())
                        .durationMs(m.durationMs())
                        .storageKey(storageKey)
                        .build();

                // INSERT
                postMapper.insertMedia(media);

                // リンク（position は i）
                TweetMedia link = TweetMedia.builder()
                        .tweetId(tweetId)
                        .mediaId(media.getMediaId())
                        .position(i)
                        .build();
                postMapper.insertTweetMedia(link);

                // レスポンス用に保持
                medias.add(media);
            }
        }

        // 親ポストの返信数を+1する
        tweetMetricsMapper.incrementReply(parentTweetId);

        PostResult postResult = tweetConverter.toPostResult(tweet, medias);
        return postResult;
    }

    // 後で共通化する

    private String guessExt(String mime) {
        if (mime == null) return ".bin";
        return switch (mime) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "video/mp4" -> ".mp4";
            case "video/quicktime" -> ".mov";
            default -> ".bin";
        };
    }

    private String inferMediaType(String mime) {
        if (mime == null) return "image";
        if (mime.startsWith("video/")) return "video";
        if ("image/gif".equals(mime)) return "gif";
        if (mime.startsWith("image/")) return "image";
        return "image";
    }

    private byte[] decodeDataUrlBytes(String dataUrl) {
        int comma = dataUrl.indexOf(',');
        String base64 = dataUrl.substring(comma + 1);
        return java.util.Base64.getDecoder().decode(base64);
    }
}
