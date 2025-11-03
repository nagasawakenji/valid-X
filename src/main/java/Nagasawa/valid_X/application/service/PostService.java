package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.validation.TweetValidator;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final TweetValidator tweetValidator;
    private final TweetConverter tweetConverter;
    private final TweetMetricsMapper tweetMetricsMapper;
    private final LocalMediaStorageService localMediaStorageService;

    @Transactional
    public PostResult post(Long userId, PostForm postForm, List<MultipartFile> mediaFiles) {

        Tweet tweet = tweetConverter.toTweet(postForm, userId);

        tweetValidator.validateContent(tweet.getContent());
        // tweetのINSERT
        postMapper.insertTweet(tweet);
        Long tweetId = tweet.getTweetId();

        // tweetMetricsのINSERT
        tweetMetricsMapper.insertInit(tweetId);

        // postResultで使う
        List<Media> medias = new ArrayList<>();



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
                String targetFileName = "tweet_" + tweetId + "_" + i + ext;

                // 4) 保存（bytes を直接渡すので二重デコード無し）
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

                // Media を作成 (現在、mediaのwidth,height, durationMsをフロント側から取得するように修正中です)
                // (現状の動作に影響がないため、ひとまずwidth, height, durationMsを省略しています)
                // 修正後にはpostFormにこれらの項目を追加します
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



        PostResult postResult = tweetConverter.toPostResult(tweet, medias);

        return postResult;
    }

    private String guessExt(String mime) {
        if (mime == null) return ".bin";
        return switch (mime) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "video/mp4" -> ".mp4";
            case "video/quicktime"-> ".mov";
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
