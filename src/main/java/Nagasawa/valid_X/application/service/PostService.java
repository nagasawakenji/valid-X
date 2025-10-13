package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.domain.dto.MediaCreate;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

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
    public PostResult post(Long userId, PostForm postForm) {

        Tweet tweet = tweetConverter.toTweet(postForm, userId);

        tweetValidator.validateContent(tweet.getContent());
        // tweetのINSERT
        postMapper.insertTweet(tweet);
        Long tweetId = tweet.getTweetId();

        // tweetMetricsのINSERT
        tweetMetricsMapper.insertInit(tweetId);

        // postResultで使う
        List<Media> medias = new ArrayList<>();



        if (postForm.medias() != null && !postForm.medias().isEmpty()) {
            for (int i = 0; i < postForm.medias().size(); i++) {
                MediaCreate m = postForm.medias().get(i);

                // dataUrl のガード
                if (m.dataUrl() == null || m.dataUrl().isBlank()) {
                    log.warn("media[{}]: dataUrl is null/blank. Skip.", i);
                    continue;
                }

                // 一度だけデコードして bytes/size を得る
                byte[] bytes;
                try {
                    bytes = decodeDataUrlBytes(m.dataUrl());
                } catch (RuntimeException e) {
                    log.warn("media[{}]: invalid dataUrl. Skip.", i, e);
                    continue;
                }
                Long size = (long) bytes.length;

                // 3) MIME と拡張子を分離して扱う
                String mimeType = m.mimeType();                 // ex: "image/jpeg" (null の可能性あり)
                String ext = guessExt(mimeType);                // ex: ".jpg"
                String targetFileName = "tweet_" + tweetId + "_" + i + ext;

                // 4) 保存（bytes を直接渡すので二重デコード無し）
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

                // Media を作成（
                Media media = Media.builder()
                        .mediaType(inferMediaType(mimeType))
                        .mimeType(mimeType)
                        .bytes(size)
                        .width(m.width())
                        .height(m.height())
                        .durationMs(m.durationMs())
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

    private byte[] decodeDataUrlBytes(String dataUrl) {
        int comma = dataUrl.indexOf(',');
        String base64 = dataUrl.substring(comma + 1);
        return java.util.Base64.getDecoder().decode(base64);
    }

}
