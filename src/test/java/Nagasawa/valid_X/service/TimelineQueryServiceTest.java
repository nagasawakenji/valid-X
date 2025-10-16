package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.application.service.TimelineQueryService;
import Nagasawa.valid_X.domain.dto.GetMediaResult;
import Nagasawa.valid_X.domain.dto.GetPostResult;
import Nagasawa.valid_X.domain.dto.Page;
import Nagasawa.valid_X.domain.dto.TweetView;
import Nagasawa.valid_X.infra.mybatis.mapper.TimelineMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class TimelineQueryServiceTest {

    @Mock
    private TimelineMapper timelineMapper;
    @Mock
    private TweetConverter tweetConverter;

    private TimelineQueryService timelineQueryService;

    @BeforeEach
    void setup() {
        timelineQueryService = new TimelineQueryService(
                timelineMapper,
                tweetConverter
        );
    }

    // homeTimeline() のテストコード
    @Test
    @DisplayName("正常系: MapperとConverterの連携により正しいPageが返却される")
    void homeTimeline_success_andCorrectField() {
        Long viewerId = 5L;
        Long tweetId = 7L;
        int limit = 30;

        TweetView tweetView = TweetView.builder()
                .tweetId(tweetId)
                .build();

        GetPostResult getPostResultRaw = GetPostResult.builder()
                .tweetId(tweetId)
                .build();
        GetMediaResult getMediaResult = GetMediaResult.builder()
                .tweetId(tweetId)
                .mediaId(10L)
                .build();

        GetPostResult getPostResult = GetPostResult.builder()
                .tweetId(tweetId)
                .media(List.of(getMediaResult))
                .build();

        when(timelineMapper.listHomeTimeline(viewerId, null, limit)).thenReturn(List.of(tweetView));
        when(tweetConverter.toGetPostResults(any())).thenReturn(List.of(getPostResultRaw));
        when(timelineMapper.listMediaForTweetIds(List.of(tweetId))).thenReturn(List.of(getMediaResult));
        when(tweetConverter.mergePostsWithMedia(List.of(getPostResultRaw), List.of(getMediaResult))).thenReturn(List.of(getPostResult));

        // 実行
        Page<GetPostResult> page = timelineQueryService.homeTimeline(viewerId, null, limit);

        // verify
        verify(timelineMapper).listHomeTimeline(viewerId, null, limit);
        verify(tweetConverter).toGetPostResults(List.of(tweetView));
        verify(timelineMapper).listMediaForTweetIds(List.of(tweetId));
        verify(tweetConverter).mergePostsWithMedia(List.of(getPostResultRaw), List.of(getMediaResult));

        // page の検証
        assertNotNull(page);
        assertThat(page.getItems().size()).isEqualTo(1);
        assertThat(page.getItems().get(0).getTweetId()).isEqualTo(tweetId);
        assertThat(page.getNextCursor()).isNull();

    }

    @Test
    @DisplayName("正常系: homeTimeline() - cursorありでnextCursorが設定される")
    void homeTimeline_withCursor() {
        Long viewerId = 5L;
        Long cursor = 10L;
        int limit = 1;

        TweetView tweet1 = TweetView.builder().tweetId(100L).build();
        TweetView tweet2 = TweetView.builder().tweetId(200L).build();

        GetPostResult post1 = GetPostResult.builder().tweetId(100L).build();
        GetPostResult post2 = GetPostResult.builder().tweetId(200L).build();
        GetMediaResult media1 = GetMediaResult.builder().tweetId(100L).mediaId(1L).build();
        GetMediaResult media2 = GetMediaResult.builder().tweetId(200L).mediaId(2L).build();

        when(timelineMapper.listHomeTimeline(viewerId, cursor, limit)).thenReturn(List.of(tweet1, tweet2));
        when(tweetConverter.toGetPostResults(List.of(tweet1, tweet2))).thenReturn(List.of(post1, post2));
        when(timelineMapper.listMediaForTweetIds(List.of(100L, 200L))).thenReturn(List.of(media1, media2));
        when(tweetConverter.mergePostsWithMedia(List.of(post1, post2), List.of(media1, media2))).thenReturn(List.of(post1, post2));

        Page<GetPostResult> page = timelineQueryService.homeTimeline(viewerId, cursor, limit);

        verify(timelineMapper).listHomeTimeline(viewerId, cursor, limit);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getNextCursor()).isEqualTo(100L);
    }



    // replies() のテストコード
    @Test
    @DisplayName("正常系: replies() - cursorなしで正しいPageが返却される")
    void replies_success_withoutCursor() {
        Long parentTweetId = 10L;
        Long viewerId = 2L;
        Long tweetId = 20L;
        int limit = 30;

        TweetView tweetView = TweetView.builder()
                .tweetId(tweetId)
                .build();
        GetPostResult getPostResultRaw = GetPostResult.builder()
                .tweetId(tweetId)
                .build();
        GetMediaResult getMediaResult = GetMediaResult.builder()
                .tweetId(tweetId)
                .mediaId(100L)
                .build();
        GetPostResult getPostResult = GetPostResult.builder()
                .tweetId(tweetId)
                .media(List.of(getMediaResult))
                .build();

        when(timelineMapper.listReplies(parentTweetId, null, limit, viewerId)).thenReturn(List.of(tweetView));
        when(tweetConverter.toGetPostResults(List.of(tweetView))).thenReturn(List.of(getPostResultRaw));
        when(timelineMapper.listMediaForTweetIds(List.of(tweetId))).thenReturn(List.of(getMediaResult));
        when(tweetConverter.mergePostsWithMedia(List.of(getPostResultRaw), List.of(getMediaResult))).thenReturn(List.of(getPostResult));

        Page<GetPostResult> page = timelineQueryService.replies(parentTweetId, null, limit, viewerId);

        verify(timelineMapper).listReplies(parentTweetId, null, limit, viewerId);
        verify(tweetConverter).toGetPostResults(List.of(tweetView));
        verify(timelineMapper).listMediaForTweetIds(List.of(tweetId));
        verify(tweetConverter).mergePostsWithMedia(List.of(getPostResultRaw), List.of(getMediaResult));

        assertNotNull(page);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getTweetId()).isEqualTo(tweetId);
        assertThat(page.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("正常系: replies() - cursorありでnextCursorが設定される")
    void replies_success_withCursor() {
        Long parentTweetId = 10L;
        Long viewerId = 2L;
        Long cursor = 50L;
        int limit = 1;

        TweetView tweetView1 = TweetView.builder().tweetId(100L).build();
        TweetView tweetView2 = TweetView.builder().tweetId(200L).build(); // extra row beyond limit

        GetPostResult post1 = GetPostResult.builder().tweetId(100L).build();
        GetPostResult post2 = GetPostResult.builder().tweetId(200L).build();

        GetMediaResult media1 = GetMediaResult.builder().tweetId(100L).mediaId(1L).build();
        GetMediaResult media2 = GetMediaResult.builder().tweetId(200L).mediaId(2L).build();

        when(timelineMapper.listReplies(parentTweetId, cursor, limit, viewerId)).thenReturn(List.of(tweetView1, tweetView2));
        when(tweetConverter.toGetPostResults(List.of(tweetView1, tweetView2))).thenReturn(List.of(post1, post2));
        when(timelineMapper.listMediaForTweetIds(List.of(100L, 200L))).thenReturn(List.of(media1, media2));
        when(tweetConverter.mergePostsWithMedia(List.of(post1, post2), List.of(media1, media2))).thenReturn(List.of(post1, post2));

        Page<GetPostResult> page = timelineQueryService.replies(parentTweetId, cursor, limit, viewerId);

        verify(timelineMapper).listReplies(parentTweetId, cursor, limit, viewerId);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getNextCursor()).isEqualTo(100L);
    }

    // userTweets() のテストコード
    @Test
    @DisplayName("正常系: userTweets() - cursorなしで正しいPageが返却される")
    void userTweets_withoutCursor() {
        Long targetUserId = 1L;
        Long viewerId = 2L;
        Long tweetId = 10L;
        int limit = 30;

        TweetView tweetView = TweetView.builder().tweetId(tweetId).build();
        GetPostResult postRaw = GetPostResult.builder().tweetId(tweetId).build();
        GetMediaResult media = GetMediaResult.builder().tweetId(tweetId).mediaId(50L).build();
        GetPostResult postMerged = GetPostResult.builder().tweetId(tweetId).media(List.of(media)).build();

        when(timelineMapper.listUserTweets(targetUserId, null, limit, viewerId)).thenReturn(List.of(tweetView));
        when(tweetConverter.toGetPostResults(List.of(tweetView))).thenReturn(List.of(postRaw));
        when(timelineMapper.listMediaForTweetIds(List.of(tweetId))).thenReturn(List.of(media));
        when(tweetConverter.mergePostsWithMedia(List.of(postRaw), List.of(media))).thenReturn(List.of(postMerged));

        Page<GetPostResult> page = timelineQueryService.userTweets(targetUserId, null, limit, viewerId);

        verify(timelineMapper).listUserTweets(targetUserId, null, limit, viewerId);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("正常系: userTweets() - cursorありでnextCursorが設定される")
    void userTweets_withCursor() {
        Long targetUserId = 1L;
        Long viewerId = 2L;
        Long cursor = 20L;
        int limit = 1;

        TweetView tweet1 = TweetView.builder().tweetId(101L).build();
        TweetView tweet2 = TweetView.builder().tweetId(202L).build();
        GetPostResult post1 = GetPostResult.builder().tweetId(101L).build();
        GetPostResult post2 = GetPostResult.builder().tweetId(202L).build();
        GetMediaResult media1 = GetMediaResult.builder().tweetId(101L).mediaId(1L).build();
        GetMediaResult media2 = GetMediaResult.builder().tweetId(202L).mediaId(2L).build();

        when(timelineMapper.listUserTweets(targetUserId, cursor, limit, viewerId)).thenReturn(List.of(tweet1, tweet2));
        when(tweetConverter.toGetPostResults(List.of(tweet1, tweet2))).thenReturn(List.of(post1, post2));
        when(timelineMapper.listMediaForTweetIds(List.of(101L, 202L))).thenReturn(List.of(media1, media2));
        when(tweetConverter.mergePostsWithMedia(List.of(post1, post2), List.of(media1, media2))).thenReturn(List.of(post1, post2));

        Page<GetPostResult> page = timelineQueryService.userTweets(targetUserId, cursor, limit, viewerId);

        verify(timelineMapper).listUserTweets(targetUserId, cursor, limit, viewerId);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getNextCursor()).isEqualTo(101L);
    }

    // popularTweets() のテストコード
    @Test
    @DisplayName("正常系: popularTweets() - cursorなしで正しいPageが返却される")
    void popularTweets_withoutCursor() {
        Long viewerId = 3L;
        Long cursorLike = null;
        Long cursorId = null;
        int limit = 30;
        int dayCount = 7;
        Long tweetId = 9L;

        TweetView tweetView = TweetView.builder().tweetId(tweetId).build();
        GetPostResult postRaw = GetPostResult.builder().tweetId(tweetId).build();
        GetMediaResult media = GetMediaResult.builder().tweetId(tweetId).mediaId(99L).build();
        GetPostResult postMerged = GetPostResult.builder().tweetId(tweetId).media(List.of(media)).build();

        when(timelineMapper.listPopularTweets(viewerId, cursorLike, cursorId, limit, dayCount)).thenReturn(List.of(tweetView));
        when(tweetConverter.toGetPostResults(List.of(tweetView))).thenReturn(List.of(postRaw));
        when(timelineMapper.listMediaForTweetIds(List.of(tweetId))).thenReturn(List.of(media));
        when(tweetConverter.mergePostsWithMedia(List.of(postRaw), List.of(media))).thenReturn(List.of(postMerged));

        Page<GetPostResult> page = timelineQueryService.popularTweets(viewerId, cursorLike, cursorId, limit, dayCount);

        verify(timelineMapper).listPopularTweets(viewerId, cursorLike, cursorId, limit, dayCount);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("正常系: popularTweets() - cursorありでnextCursorが設定される")
    void popularTweets_withCursor() {
        Long viewerId = 3L;
        Long cursorLike = 50L;
        Long cursorId = 30L;
        int limit = 1;
        int dayCount = 7;

        TweetView tweet1 = TweetView.builder().tweetId(100L).build();
        TweetView tweet2 = TweetView.builder().tweetId(200L).build();
        GetPostResult post1 = GetPostResult.builder().tweetId(100L).build();
        GetPostResult post2 = GetPostResult.builder().tweetId(200L).build();
        GetMediaResult media1 = GetMediaResult.builder().tweetId(100L).mediaId(1L).build();
        GetMediaResult media2 = GetMediaResult.builder().tweetId(200L).mediaId(2L).build();

        when(timelineMapper.listPopularTweets(viewerId, cursorLike, cursorId, limit, dayCount)).thenReturn(List.of(tweet1, tweet2));
        when(tweetConverter.toGetPostResults(List.of(tweet1, tweet2))).thenReturn(List.of(post1, post2));
        when(timelineMapper.listMediaForTweetIds(List.of(100L, 200L))).thenReturn(List.of(media1, media2));
        when(tweetConverter.mergePostsWithMedia(List.of(post1, post2), List.of(media1, media2))).thenReturn(List.of(post1, post2));

        Page<GetPostResult> page = timelineQueryService.popularTweets(viewerId, cursorLike, cursorId, limit, dayCount);

        verify(timelineMapper).listPopularTweets(viewerId, cursorLike, cursorId, limit, dayCount);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getNextCursor()).isEqualTo(100L);
    }
}
