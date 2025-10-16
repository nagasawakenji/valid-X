package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.application.service.LocalMediaStorageService;
import Nagasawa.valid_X.application.service.PostService;
import Nagasawa.valid_X.domain.dto.MediaCreate;
import Nagasawa.valid_X.domain.dto.MediaResult;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.validation.TweetValidator;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostMapper postMapper;
    @Mock
    private TweetValidator tweetValidator;
    @Mock
    private TweetConverter tweetConverter;
    @Mock
    private TweetMetricsMapper tweetMetricsMapper;
    @Mock
    private LocalMediaStorageService localMediaStorageService;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("正常系: メディアなし投稿が成功する")
    void post_withoutMedia_success() {
        Long userId = 1L;
        Long tweetId = 10L;
        Tweet tweet = Tweet.builder().tweetId(tweetId).content("hello").build();
        PostForm form = new PostForm("hello", null, List.of());

        PostResult returnedResult = PostResult.builder()
                .tweetId(tweetId)
                .content("hello")
                .userId(userId)
                .build();

        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(returnedResult);

        PostResult result = postService.post(userId, form);

        verify(tweetValidator).validateContent("hello");
        verify(postMapper).insertTweet(tweet);
        verify(tweetMetricsMapper).insertInit(tweetId);
        verify(tweetConverter).toPostResult(tweet, List.of());
        verifyNoInteractions(localMediaStorageService);

        // resultの検証
        assertThat(result.getTweetId()).isEqualTo(tweetId);
        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getMedias()).isNull();
    }

    @Test
    @DisplayName("正常系: メディア付き投稿が成功する")
    void post_withMedia_success() {
        Long userId = 2L;
        Long tweetId = 100L;

        // base64部分が有効な文字列である必要がある
        String base64 = java.util.Base64.getEncoder().encodeToString("mockdata".getBytes());
        String dataUrl = "data:image/png;base64," + base64;

        MediaCreate mediaCreate = new MediaCreate(dataUrl, "image/jpeg", 100, 100, null);
        PostForm postForm = new PostForm("image tweet", null, List.of(mediaCreate));

        MediaResult mediaResult = MediaResult.builder()
                .mediaId(1L)
                .mediaType("image")
                .mimeType(mediaCreate.mimeType())
                .height(mediaCreate.height())
                .width(mediaCreate.width())
                .storageKey("key")
                .build();

        PostResult returnedResult = PostResult.builder()
                .tweetId(tweetId)
                .content("image tweet")
                .userId(userId)
                .medias(List.of(mediaResult))
                .build();

        Tweet tweet = Tweet.builder().tweetId(tweetId).content("image tweet").build();

        when(tweetConverter.toTweet(postForm, userId)).thenReturn(tweet);
        when(localMediaStorageService.saveBytes(any(), anyString())).thenReturn("key");
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(returnedResult);

        PostResult result = postService.post(userId, postForm);

        // 各処理の呼び出し確認
        verify(tweetValidator).validateContent("image tweet");
        verify(postMapper).insertTweet(tweet);
        verify(tweetMetricsMapper).insertInit(tweetId);
        verify(localMediaStorageService).saveBytes(any(), contains("tweet_" + tweetId));
        verify(postMapper).insertMedia(any(Media.class));
        verify(postMapper).insertTweetMedia(any());
        verify(tweetConverter).toPostResult(eq(tweet), anyList());

        // resultの検証
        assertThat(result.getTweetId()).isEqualTo(tweetId);
        assertThat(result.getContent()).isEqualTo("image tweet");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getMedias().get(0))
                .usingRecursiveComparison()
                .isEqualTo(mediaResult);
    }

    @Test
    @DisplayName("異常系: dataUrlが不正な場合はスキップされる")
    void post_invalidDataUrl_skipped() {
        Long userId = 3L;
        Long tweetId = 200L;
        String invalidDataUrl = "data:image/png;base64,%%%"; // base64が壊れている

        MediaCreate mediaCreate = new MediaCreate(invalidDataUrl, "image/jpeg", 100, 100, null);
        PostForm postForm = new PostForm("invalid", null, List.of(mediaCreate));

        PostResult returnedResult = PostResult.builder()
                .tweetId(tweetId)
                .content("invalid")
                .userId(userId)
                .build();

        Tweet tweet = Tweet.builder().tweetId(tweetId).content("invalid").build();
        when(tweetConverter.toTweet(postForm, userId)).thenReturn(tweet);
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(returnedResult);

        PostResult result = postService.post(userId, postForm);

        verify(tweetValidator).validateContent("invalid");
        verify(postMapper).insertTweet(tweet);
        verify(tweetMetricsMapper).insertInit(tweetId);
        verify(tweetConverter).toPostResult(eq(tweet), eq(List.of()));
        verifyNoInteractions(localMediaStorageService);

        // resultの検証
        assertThat(result.getTweetId()).isEqualTo(tweetId);
        assertThat(result.getContent()).isEqualTo("invalid");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getMedias()).isNull();
    }
}