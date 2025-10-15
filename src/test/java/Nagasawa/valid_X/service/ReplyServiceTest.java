package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.application.service.LocalMediaStorageService;
import Nagasawa.valid_X.application.service.ReplyService;
import Nagasawa.valid_X.domain.dto.MediaCreate;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
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

import java.time.Clock;
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

    private Clock clock;
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

    @Test
    @DisplayName("正常系: メディアなしでtweetとmediaが保存される")
    void reply_withoutMedia_success() {
        Long parentId = 10L;
        Long tweetId = 15L;
        Long userId = 1L;
        Instant now = Instant.parse("2025-10-14T12:00:00Z");
        PostForm form = new PostForm("TestReply", parentId, List.of());

        Tweet tweet = new Tweet(tweetId, userId, form.content(), form.inReplyToTweet(), now);
        PostResult expected = new PostResult(tweetId, userId, "TestReply", parentId, now, List.of());

        when(replyMapper.parentExists(parentId)).thenReturn(true);
        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(expected);

        PostResult result = replyService.reply(parentId, userId, form);

        verify(tweetValidator).validateContent("TestReply");
        verify(postMapper).insertTweet(tweet);
        verify(tweetMetricsMapper).insertInit(tweetId);
        verify(tweetMetricsMapper).incrementReply(parentId);
        verify(postMapper, never()).insertMedia(any());
        verify(postMapper, never()).insertTweetMedia(any());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("異常系: 親ツイートが存在しない場合はIllegalArgumentExceptionがスローされる")
    void reply_parentNotFound_throwsException() {
        Long parentId = 99L;
        Long userId = 1L;
        PostForm form = new PostForm("Reply", parentId, List.of());

        when(replyMapper.parentExists(parentId)).thenReturn(false);

        assertThatThrownBy(() -> replyService.reply(parentId, userId, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("parent tweet not found: id=" + parentId);

        verifyNoInteractions(postMapper, tweetMetricsMapper);
    }

    @Test
    @DisplayName("正常系: メディア付きでMediaとTweetMediaが登録される")
    void reply_withMedia_success() {
        Long parentId = 10L;
        Long tweetId = 20L;
        Long userId = 1L;

        // base64部分が有効な文字列である必要がある
        String base64 = java.util.Base64.getEncoder().encodeToString("mockdata".getBytes());
        String dataUrl = "data:image/png;base64," + base64;

        // メディアフォームの作成
        var mediaCreate = new MediaCreate(
                dataUrl, "image/png", 100, 100, null
        );

        // PostForm
        PostForm form = new PostForm("with image", parentId, List.of(mediaCreate));

        Tweet tweet = new Tweet(tweetId, userId, "with image", parentId, Instant.now());
        PostResult expected = new PostResult(tweetId, userId, "with image", parentId, Instant.now(), List.of());

        when(replyMapper.parentExists(parentId)).thenReturn(true);
        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(localMediaStorageService.saveBytes(any(), anyString())).thenReturn("stored-key");
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(expected);

        // 実行
        PostResult result = replyService.reply(parentId, userId, form);

        // 確認
        verify(postMapper).insertMedia(any(Media.class));
        verify(postMapper).insertTweetMedia(any());
        verify(tweetMetricsMapper).incrementReply(parentId);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("異常系: dataUrlが壊れている場合はスキップされる")
    void reply_invalidDataUrl_skip() {
        Long parentId = 10L;
        Long tweetId = 25L;
        Long userId = 1L;

        String invalidDataUrl = "data:image/png;base64,%%%"; // base64が壊れている
        var mediaCreate = new MediaCreate(invalidDataUrl, "image/png", 100, 100, null);
        PostForm form = new PostForm("invalid", parentId, List.of(mediaCreate));

        Tweet tweet = new Tweet(tweetId, userId, "invalid", parentId, Instant.now());
        PostResult expected = new PostResult(tweetId, userId, "invalid", parentId, Instant.now(), List.of());

        when(replyMapper.parentExists(parentId)).thenReturn(true);
        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(expected);

        PostResult result = replyService.reply(parentId, userId, form);

        // 無効なdataUrlのためinsertMediaは呼ばれない
        verify(postMapper, never()).insertMedia(any());
        verify(postMapper, never()).insertTweetMedia(any());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("異常系: saveBytesが例外を投げた場合はスキップされる")
    void reply_saveBytesThrows_skip() {
        Long parentId = 10L;
        Long tweetId = 30L;
        Long userId = 1L;

        String base64 = java.util.Base64.getEncoder().encodeToString("mockdata".getBytes());
        String dataUrl = "data:image/png;base64," + base64;

        var mediaCreate = new MediaCreate(dataUrl, "image/png", 100, 100, null);
        PostForm form = new PostForm("error", parentId, List.of(mediaCreate));

        Tweet tweet = new Tweet(tweetId, userId, "error", parentId, Instant.now());
        PostResult expected = new PostResult(tweetId, userId, "error", parentId, Instant.now(), List.of());

        when(replyMapper.parentExists(parentId)).thenReturn(true);
        when(tweetConverter.toTweet(form, userId)).thenReturn(tweet);
        when(localMediaStorageService.saveBytes(any(), anyString()))
                .thenThrow(new RuntimeException("save failed"));
        when(tweetConverter.toPostResult(eq(tweet), anyList())).thenReturn(expected);

        PostResult result = replyService.reply(parentId, userId, form);

        // 例外は握りつぶされる（スキップされる）
        verify(postMapper, never()).insertMedia(any());
        verify(postMapper, never()).insertTweetMedia(any());
        assertThat(result).isEqualTo(expected);
    }
}
