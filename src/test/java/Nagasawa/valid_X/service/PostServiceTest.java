package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.application.service.LocalMediaStorageService;
import Nagasawa.valid_X.application.service.PostService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // MultiPartFileをmock化するヘルパメソッド
    private MultipartFile createMockMultiPartFile(
            String mimeType,
            long size,
            String fileName,
            byte[] bytes
    ) throws IOException {
        MultipartFile mockMf = mock(MultipartFile.class);
        when(mockMf.getContentType()).thenReturn(mimeType);
        when(mockMf.isEmpty()).thenReturn(false);
        // postService内で mf.getBytes() を使っているため、設定を維持
        when(mockMf.getBytes()).thenReturn(bytes);
        when(mockMf.getSize()).thenReturn(size);
        // Serviceのロジックで getOriginalFilename() を使っていないため、これは不要（エラー回避のため）
        // when(mockMf.getOriginalFilename()).thenReturn(fileName);

        return mockMf;
    }

    @Test
    @DisplayName("正常系: メディアなし投稿が成功する (PostForm修正後)")
    void post_withoutMedia_success() {
        Long userId = 1L;
        Long tweetId = 10L;
        Tweet tweet = Tweet.builder().tweetId(tweetId).content("hello").build();
        // ★ 修正: PostForm DTOの引数を削除 (PostForm(content, inReplyToTweet)を想定)
        PostForm form = new PostForm("hello", null);

        PostResult returnedResult = PostResult.builder()
                .tweetId(tweetId)
                .content("hello")
                .userId(userId)
                .build();

        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(returnedResult);

        // ★ 修正: postService.post のシグネチャ変更 (mediaFiles引数を追加)
        PostResult result = postService.post(userId, form, List.of());

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
    @DisplayName("正常系: メディア付き投稿が成功する (ファイル分離後)")
    void post_withMedia_success() throws IOException {
        Long userId = 2L;
        Long tweetId = 100L;
        Long mediaId = 1L; // INSERT時に生成されるIDを想定

        byte[] fileContent = "mock image data".getBytes();
        MultipartFile mockMf = createMockMultiPartFile(
                "image/jpeg",
                (long) fileContent.length,
                "test_image.jpg", // ファイル名はロジックで使われないがヘルパーには必要
                fileContent
        );
        // ★ 修正: PostForm DTOの引数を削除 (PostForm(content, inReplyToTweet)を想定)
        PostForm postForm = new PostForm("image tweet", null);
        // ★ ファイルリストを引数として渡す準備
        List<MultipartFile> mediaFiles = List.of(mockMf);

        // ★ MediaResultからwidth/height/durationMsは削除済みを想定
        MediaResult mediaResult = MediaResult.builder()
                .mediaId(mediaId)
                .mediaType("image")
                .mimeType(mockMf.getContentType())
                .storageKey("key")
                .build();

        PostResult returnedResult = PostResult.builder()
                .tweetId(tweetId)
                .content("image tweet")
                .userId(userId)
                .medias(List.of(mediaResult))
                .build();

        Tweet tweet = Tweet.builder().tweetId(tweetId).content("image tweet").build();

        // Mock設定
        when(tweetConverter.toTweet(postForm, userId)).thenReturn(tweet);
        // ★ 修正: saveBytes の代わりに save(MultipartFile) が呼ばれるように設定
        when(localMediaStorageService.save(mockMf)).thenReturn("key");
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(returnedResult);

        // insertMediaでmediaIdが設定されるように設定
        doAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setMediaId(mediaId);
            return null;
        }).when(postMapper).insertMedia(any(Media.class));

        // ★ 修正: postService.post のシグネチャ変更 (mediaFiles引数を追加)
        PostResult result = postService.post(userId, postForm, mediaFiles);

        // 各処理の呼び出し確認
        verify(tweetValidator).validateContent("image tweet");
        verify(postMapper).insertTweet(tweet);
        verify(tweetMetricsMapper).insertInit(tweetId);
        // ★ 修正: save(MultipartFile) が呼ばれたことを確認
        verify(localMediaStorageService).save(mockMf);
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
}
