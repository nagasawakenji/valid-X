package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.LikeService;
import Nagasawa.valid_X.domain.dto.LikeStatusResult;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.LikeMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class LikeServiceTest {

    @Mock
    private TweetMetricsMapper tweetMetricsMapper;
    @Mock
    private LikeMapper likeMapper;

    private LikeService likeService;

    @BeforeEach
    void setup() {
        likeService = new LikeService(
                tweetMetricsMapper,
                likeMapper
        );
    }

    /** like() に関するテストコード */
    // mapperでの処理が大部分なので、このテストコードがどのくらい有効なのかは議論の余地があるかも
    @Test
    @DisplayName("正常系: いいねをしていないツイートに対して、likeが正しく実行できる")
    void like_success() {
        long tweetId = 8L;
        long userId = 5L;

        // likeコネクションの設定
        when(likeMapper.insert(userId, tweetId)).thenReturn(1);
        when(tweetMetricsMapper.getLikeCount(tweetId)).thenReturn(7);

        LikeStatusResult result = likeService.like(tweetId, userId);

        verify(tweetMetricsMapper).incrementLike(tweetId);
        verify(tweetMetricsMapper).getLikeCount(tweetId);
        assertThat(result.getLikeCount()).isEqualTo(7);
        assertThat(result.isLiked()).isTrue();
    }

    @Test
    @DisplayName("正常系: すでにいいねをしているツイートに対して、冪等性を保持する")
    void like_already() {
        long tweetId = 8L;
        long userId = 5L;

        when(likeMapper.insert(userId, tweetId)).thenReturn(0);
        when(tweetMetricsMapper.getLikeCount(tweetId)).thenReturn(6);

        LikeStatusResult result = likeService.like(tweetId, userId);

        verify(tweetMetricsMapper, never()).incrementLike(tweetId);
        verify(tweetMetricsMapper).getLikeCount(tweetId);
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("異常系: DataIntegrityViolationException発生時にNotFoundProblemExceptionが送出される")
    void like_tweetNotFound() {
        long tweetId = 8L;
        long userId = 5L;

        when(likeMapper.insert(userId, tweetId)).thenThrow(new DataIntegrityViolationException("FK error"));

        assertThatThrownBy(() -> likeService.like(tweetId, userId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("tweet not found: id=" + tweetId);
    }

    /** unlike() に対するテストコード */
    @Test
    @DisplayName("正常系: すでにいいねをしているツイートに対して、正しくunlike()が実行できる")
    void unlike_success() {
        long tweetId = 8L;
        long userId = 5L;

        when(likeMapper.delete(userId, tweetId)).thenReturn(1);
        when(tweetMetricsMapper.getLikeCount(tweetId)).thenReturn(5);

        LikeStatusResult result = likeService.unlike(tweetId, userId);

        verify(tweetMetricsMapper).decrementLike(tweetId);
        verify(tweetMetricsMapper).getLikeCount(tweetId);
        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("正常系: まだいいねをしていないツイートに対して、冪等性を保持する")
    void unlike_yetLiked() {
        long tweetId = 8L;
        long userId = 5L;

        when(likeMapper.delete(userId, tweetId)).thenReturn(0);
        when(tweetMetricsMapper.getLikeCount(tweetId)).thenReturn(6);

        LikeStatusResult result = likeService.unlike(tweetId, userId);

        verify(tweetMetricsMapper, never()).decrementLike(tweetId);
        verify(tweetMetricsMapper).getLikeCount(tweetId);
        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("異常系: DataIntegrityViolationException発生時にNotFoundProblemExceptionが送出される")
    void unlike_tweetNotFound() {
        long tweetId = 8L;
        long userId = 5L;

        when(likeMapper.delete(userId, tweetId)).thenThrow(new DataIntegrityViolationException("FK error"));

        assertThatThrownBy(() -> likeService.unlike(tweetId, userId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("tweet not found: id=" + tweetId);

    }
}
