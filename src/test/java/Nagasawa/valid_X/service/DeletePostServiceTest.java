package Nagasawa.valid_X.service;


import Nagasawa.valid_X.application.service.DeletePostService;
import Nagasawa.valid_X.domain.dto.DeletePostResult;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.DeleteMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DeletePostServiceTest {

    @Mock
    private TweetMapper tweetMapper;

    @Mock
    private DeleteMapper deleteMapper;

    @InjectMocks
    private DeletePostService deletePostService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("正常系: tweetが存在しuserIdが一致する場合、全削除処理が行われる")
    void delete_success() {
        Long tweetId = 10L;
        Long userId = 5L;

        // tweetが存在するケース
        Tweet tweet = Tweet.builder()
                .tweetId(tweetId)
                .userId(userId)
                .content("test content")
                .build();

        when(tweetMapper.selectTweetById(tweetId)).thenReturn(tweet);
        when(deleteMapper.deleteTweetById(tweetId)).thenReturn(1);
        when(deleteMapper.deleteLikeById(tweetId)).thenReturn(2);
        when(deleteMapper.deleteRepostById(tweetId)).thenReturn(3);
        when(deleteMapper.deleteTweetMetricsById(tweetId)).thenReturn(1);

        DeletePostResult result = deletePostService.delete(tweetId, userId);

        // 各delete処理が呼ばれていることを検証
        verify(deleteMapper).deleteTweetById(tweetId);
        verify(deleteMapper).deleteLikeById(tweetId);
        verify(deleteMapper).deleteRepostById(tweetId);
        verify(deleteMapper).deleteTweetMetricsById(tweetId);

        // 結果オブジェクトの検証
        assertThat(result).isNotNull();
        assertThat(result.getTweetId()).isEqualTo(tweetId);
        assertThat(result.getDeletedTweet()).isEqualTo(1);
        assertThat(result.getDeletedLikes()).isEqualTo(2);
        assertThat(result.getDeletedReposts()).isEqualTo(3);
        assertThat(result.getDeletedMetrics()).isEqualTo(1);
    }

    @Test
    @DisplayName("異常系: tweetが存在しない場合はNotFoundProblemExceptionをスローする")
    void delete_tweetNotFound() {
        Long tweetId = 99L;
        Long userId = 1L;

        when(tweetMapper.selectTweetById(tweetId)).thenReturn(null);

        assertThatThrownBy(() -> deletePostService.delete(tweetId, userId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessageContaining("post is not found");

        // Mapper呼び出しが行われないこと
        verify(deleteMapper, never()).deleteTweetById(anyLong());
    }

    @Test
    @DisplayName("異常系: userIdが一致しない場合はIllegalArgumentExceptionをスローする")
    void delete_userMismatch() {
        Long tweetId = 10L;
        Long userId = 99L;

        Tweet tweet = Tweet.builder()
                .tweetId(tweetId)
                .userId(1L)
                .content("test content")
                .build();

        when(tweetMapper.selectTweetById(tweetId)).thenReturn(tweet);

        assertThatThrownBy(() -> deletePostService.delete(tweetId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("user not matched");

        // deleteMapperが呼ばれていないことを確認
        verify(deleteMapper, never()).deleteTweetById(anyLong());
    }
}