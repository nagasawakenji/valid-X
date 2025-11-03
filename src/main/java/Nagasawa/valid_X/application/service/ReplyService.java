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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    // 修正: ファイルリスト (mediaFiles) を引数に追加
    public PostResult reply(Long parentTweetId, Long userId, PostForm postForm, List<MultipartFile> mediaFiles) {

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

        // mediaFilesから送られてきたMultiPartFileを処理
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (int i = 0; i < mediaFiles.size(); i++) {
                MultipartFile mf = mediaFiles.get(i);

                // mfが空出ないかのチェック
                if (mf.isEmpty()) {
                    log.warn("media[{}]: MultipartFile is empty. Skip.", i);
                    continue;
                }

                // mfのサイズを取得する
                Long size = mf.getSize();

                // MIME と拡張子を分離して扱う
                String mimeType = mf.getContentType();       // ex: "image/jpeg" (null の可能性あり)
                String ext = guessExt(mimeType);                // ex: ".jpg"
                // targetFileName は LocalMediaStorageService.save() が内部でUUIDを使っているので不要だが、ログのために残す
                String targetFileName = "tweet_" + tweetId + "_" + i + ext;

                // 4) 保存（最も効率的な save(MultipartFile) を使用）
                String storageKey;
                try {
                    storageKey = localMediaStorageService.save(mf);
                } catch (RuntimeException e) {
                    log.warn("media[{}]: persist failed. Skip linking.", i, e);
                    continue;
                }
                if (storageKey == null || storageKey.isBlank()) {
                    log.warn("media[{}]: storageKey empty. Skip linking.", i);
                    continue;
                }

                // Media を作成 (メタデータは null のまま)
                Media media = Media.builder()
                        .mediaType(inferMediaType(mimeType))
                        .mimeType(mimeType)
                        .bytes(size)
                        .width(null)
                        .height(null)
                        .durationMs(null)
                        .storageKey(storageKey)
                        .build();

                // INSERT & リンク
                postMapper.insertMedia(media);

                TweetMedia tweetMedia = TweetMedia.builder()
                        .tweetId(tweetId)
                        .mediaId(media.getMediaId())
                        .position(i)
                        .build();
                postMapper.insertTweetMedia(tweetMedia);

                // レスポンス用に詰める
                medias.add(media);
            }
        }

        // 親ポストの返信数を+1する
        tweetMetricsMapper.incrementReply(parentTweetId);

        PostResult postResult = tweetConverter.toPostResult(tweet, medias);
        return postResult;
    }

    // ... (guessExt, inferMediaType メソッドは変更なし) ...

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
}
