package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.RepostService;
import Nagasawa.valid_X.domain.dto.RepostStatusResult;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.RepostMapper;
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

/**
 * RepostService に対する単体テスト
 * mapper層をモック化して、正常系・異常系の全パターンを検証
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class RepostServiceTest {

    @Mock
    private RepostMapper repostMapper;
    @Mock
    private TweetMetricsMapper tweetMetricsMapper;

    private RepostService repostService;

    @BeforeEach
    void setup() {
        repostService = new RepostService(repostMapper, tweetMetricsMapper);
    }

    // repost() のテスト
    @Test
    @DisplayName("正常系: まだリポストしていないツイートに対して、repost() が正しく実行できる")
    void repost_success() {
        long tweetId = 10L;
        long userId = 3L;

        when(repostMapper.insert(userId, tweetId)).thenReturn(1);
        when(tweetMetricsMapper.getRepostCount(tweetId)).thenReturn(4);

        RepostStatusResult result = repostService.repost(tweetId, userId);

        // 確認
        verify(tweetMetricsMapper).incrementRepost(tweetId);
        verify(tweetMetricsMapper).getRepostCount(tweetId);
        assertThat(result.getRepostCount()).isEqualTo(4L);
        assertThat(result.isReposted()).isTrue();
    }

    @Test
    @DisplayName("正常系: すでにリポスト済みの場合は冪等性が保たれる（incrementされない）")
    void repost_already() {
        long tweetId = 10L;
        long userId = 3L;

        when(repostMapper.insert(userId, tweetId)).thenReturn(0);
        when(tweetMetricsMapper.getRepostCount(tweetId)).thenReturn(5);

        RepostStatusResult result = repostService.repost(tweetId, userId);

        verify(tweetMetricsMapper, never()).incrementRepost(tweetId);
        verify(tweetMetricsMapper).getRepostCount(tweetId);
        assertThat(result.isReposted()).isTrue();
        assertThat(result.getRepostCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("異常系: repost時にDataIntegrityViolationExceptionが発生した場合、NotFoundProblemExceptionがスローされる")
    void repost_tweetNotFound() {
        long tweetId = 10L;
        long userId = 3L;

        when(repostMapper.insert(userId, tweetId))
                .thenThrow(new DataIntegrityViolationException("FK violation"));

        assertThatThrownBy(() -> repostService.repost(tweetId, userId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("tweet not found: id=" + tweetId);
    }


    // unrepost() のテスト
    @Test
    @DisplayName("正常系: リポスト済みのツイートに対して、unrepost() が正しく実行できる")
    void unrepost_success() {
        long tweetId = 10L;
        long userId = 3L;

        when(repostMapper.delete(userId, tweetId)).thenReturn(1);
        when(tweetMetricsMapper.getRepostCount(tweetId)).thenReturn(2);

        RepostStatusResult result = repostService.unrepost(tweetId, userId);

        verify(tweetMetricsMapper).decrementRepost(tweetId);
        verify(tweetMetricsMapper).getRepostCount(tweetId);
        assertThat(result.isReposted()).isFalse();
        assertThat(result.getRepostCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("正常系: まだリポストしていない場合は冪等性が保たれる（decrementされない）")
    void unrepost_alreadyUnreposted() {
        long tweetId = 10L;
        long userId = 3L;

        when(repostMapper.delete(userId, tweetId)).thenReturn(0);
        when(tweetMetricsMapper.getRepostCount(tweetId)).thenReturn(1);

        RepostStatusResult result = repostService.unrepost(tweetId, userId);

        verify(tweetMetricsMapper, never()).decrementRepost(tweetId);
        verify(tweetMetricsMapper).getRepostCount(tweetId);
        assertThat(result.isReposted()).isFalse();
        assertThat(result.getRepostCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("異常系: unrepost時にDataIntegrityViolationExceptionが発生した場合、NotFoundProblemExceptionがスローされる")
    void unrepost_tweetNotFound() {
        long tweetId = 10L;
        long userId = 3L;

        when(repostMapper.delete(userId, tweetId))
                .thenThrow(new DataIntegrityViolationException("FK violation"));

        assertThatThrownBy(() -> repostService.unrepost(tweetId, userId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("tweet not found: id=" + tweetId);
    }
}