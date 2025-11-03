package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.application.service.LocalMediaStorageService;
import Nagasawa.valid_X.application.service.ReplyService;
import Nagasawa.valid_X.domain.dto.MediaResult;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.validation.TweetValidator;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.ReplyMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class ReplyServiceTest {

    @Mock
    private ReplyMapper replyMapper;
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

    @Captor
    private ArgumentCaptor<Media> mediaCaptor;

    private ReplyService replyService;

    @BeforeEach
    void setup() {
        replyService = new ReplyService(
                replyMapper,
                postMapper,
                tweetValidator,
                tweetConverter,
                tweetMetricsMapper,
                localMediaStorageService
        );
    }

    /**
     * MultipartFileをmock化するヘルパメソッド
     */
    private MultipartFile createMockMultiPartFile(
            String mimeType,
            long size,
            byte[] bytes
    ) throws IOException {
        MultipartFile mockMf = mock(MultipartFile.class);
        when(mockMf.getContentType()).thenReturn(mimeType);
        when(mockMf.isEmpty()).thenReturn(false);
        when(mockMf.getSize()).thenReturn(size);

        return mockMf;
    }

    @Test
    @DisplayName("正常系: メディアなしリプライが成功する (シグネチャ変更後)")
    void reply_withoutMedia_success() {
        Long parentId = 10L;
        Long tweetId = 15L;
        Long userId = 1L;
        Instant now = Instant.parse("2025-10-14T12:00:00Z");

        // ★ 修正: PostForm DTOの引数をcontentとparentIdのみにする (メディアフィールド削除)
        PostForm form = new PostForm("TestReply", parentId);
        // ファイルリストは空として渡す
        List<MultipartFile> mediaFiles = List.of();

        Tweet tweet = new Tweet(tweetId, userId, form.content(), form.inReplyToTweet(), now);
        PostResult expected = new PostResult(tweetId, userId, "TestReply", parentId, now, List.of());

        when(replyMapper.parentExists(parentId)).thenReturn(true);
        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(expected);

        // ★ 修正: Service呼び出しを4引数に変更
        PostResult result = replyService.reply(parentId, userId, form, mediaFiles);

        verify(tweetValidator).validateContent("TestReply");
        verify(postMapper).insertTweet(tweet);
        verify(tweetMetricsMapper).insertInit(tweetId);
        verify(tweetMetricsMapper).incrementReply(parentId);
        verify(postMapper, never()).insertMedia(any());
        verify(postMapper, never()).insertTweetMedia(any());
        verifyNoInteractions(localMediaStorageService); // ファイルがないため、ストレージ処理は呼ばれない
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("異常系: 親ツイートが存在しない場合はIllegalArgumentExceptionがスローされる")
    void reply_parentNotFound_throwsException() {
        Long parentId = 99L;
        Long userId = 1L;
        // ★ 修正: PostForm DTOの引数をcontentとparentIdのみにする
        PostForm form = new PostForm("Reply", parentId);

        when(replyMapper.parentExists(parentId)).thenReturn(false);

        // ★ 修正: Service呼び出しを4引数に変更
        assertThatThrownBy(() -> replyService.reply(parentId, userId, form, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("parent tweet not found: id=" + parentId);

        verifyNoInteractions(postMapper, tweetMetricsMapper, localMediaStorageService);
    }

    @Test
    @DisplayName("正常系: メディア付きでMediaとTweetMediaが登録される (ファイル分離後)")
    void reply_withMedia_success() throws IOException {
        Long parentId = 10L;
        Long tweetId = 20L;
        Long userId = 1L;
        Long mediaId = 1L; // 生成されるMedia ID

        byte[] fileContent = "mock image data".getBytes();
        MultipartFile mockMf = createMockMultiPartFile(
                "image/jpeg",
                (long) fileContent.length,
                fileContent
        );

        // ★ 修正: PostForm DTOの引数をcontentとparentIdのみにする
        PostForm form = new PostForm("with image", parentId);
        // ファイルリストを引数として渡す準備
        List<MultipartFile> mediaFiles = List.of(mockMf);


        Tweet tweet = new Tweet(tweetId, userId, "with image", parentId, Instant.now());

        // PostResultの期待値を作成
        MediaResult mediaResult = MediaResult.builder()
                .mediaId(mediaId)
                .mediaType("image")
                .mimeType("image/jpeg")
                .bytes((long) fileContent.length)
                .storageKey("stored-key")
                .build();
        PostResult expected = new PostResult(tweetId, userId, "with image", parentId, Instant.now(), List.of(mediaResult));

        when(replyMapper.parentExists(parentId)).thenReturn(true);
        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        // ★ 修正: saveBytes の代わりに save(MultipartFile) が呼ばれるように設定
        when(localMediaStorageService.save(mockMf)).thenReturn("stored-key");
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(expected);

        // postMapper.insertMediaが呼ばれた際に、mediaオブジェクトにIDを設定する
        doAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setMediaId(mediaId);
            return null;
        }).when(postMapper).insertMedia(any(Media.class));


        // ★ 修正: Service呼び出しを4引数に変更
        PostResult result = replyService.reply(parentId, userId, form, mediaFiles);

        // 確認
        verify(postMapper).insertMedia(any(Media.class));
        verify(postMapper).insertTweetMedia(any());
        verify(tweetMetricsMapper).incrementReply(parentId);
        assertThat(result).isEqualTo(expected);
    }

}
