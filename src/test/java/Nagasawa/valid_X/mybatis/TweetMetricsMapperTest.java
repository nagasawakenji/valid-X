package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.TweetMetrics;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMapper;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class TweetMetricsMapperTest {

    @Autowired
    private TweetMetricsMapper tweetMetricsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;

    private User user;
    private Tweet tweet;

    @BeforeEach
    void setup() {
        Instant fixedTime = Instant.parse("2025-10-13T12:00:00Z");

        // ユーザ作成
        user = User.builder()
                .username("test_user")
                .displayName("Test User")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(user);
        assertThat(user.getId()).isNotNull();

        // ツイート作成
        tweet = Tweet.builder()
                .userId(user.getId())
                .content("Hello World")
                .createdAt(fixedTime)
                .build();
        postMapper.insertTweet(tweet);
        assertThat(tweet.getTweetId()).isNotNull();

        // metrics初期行を作成
        tweetMetricsMapper.insertInit(tweet.getTweetId());
    }

    @Test
    @DisplayName("正常系: 初期行が作成される")
    void insertInit_success() {
        int result = tweetMetricsMapper.insertInit(tweet.getTweetId());
        assertThat(result).isEqualTo(0); // 既に存在しているため DO NOTHING
    }

    @Test
    @DisplayName("正常系: findByTweetIdで正しく取得できる")
    void findByTweetId_success() {
        TweetMetrics metrics = tweetMetricsMapper.findByTweetId(tweet.getTweetId());
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTweetId()).isEqualTo(tweet.getTweetId());
        assertThat(metrics.getLikeCount()).isEqualTo(0);
        assertThat(metrics.getRepostCount()).isEqualTo(0);
        assertThat(metrics.getReplyCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("正常系: likeCountを+1できる")
    void incrementLike_success() {
        tweetMetricsMapper.incrementLike(tweet.getTweetId());
        int likeCount = tweetMetricsMapper.getLikeCount(tweet.getTweetId());
        assertThat(likeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: likeCountを-1（下限0）できる")
    void decrementLike_success() {
        tweetMetricsMapper.incrementLike(tweet.getTweetId());
        tweetMetricsMapper.decrementLike(tweet.getTweetId());
        int likeCount = tweetMetricsMapper.getLikeCount(tweet.getTweetId());
        assertThat(likeCount).isEqualTo(0);

        // 0からさらに-1しても0のまま
        tweetMetricsMapper.decrementLike(tweet.getTweetId());
        int likeCount2 = tweetMetricsMapper.getLikeCount(tweet.getTweetId());
        assertThat(likeCount2).isEqualTo(0);
    }

    @Test
    @DisplayName("正常系: repostCountを+1/-1できる")
    void incrementDecrementRepost_success() {
        tweetMetricsMapper.incrementRepost(tweet.getTweetId());
        assertThat(tweetMetricsMapper.getRepostCount(tweet.getTweetId())).isEqualTo(1);

        tweetMetricsMapper.decrementRepost(tweet.getTweetId());
        assertThat(tweetMetricsMapper.getRepostCount(tweet.getTweetId())).isEqualTo(0);
    }

    @Test
    @DisplayName("正常系: replyCountを+1/-1できる")
    void incrementDecrementReply_success() {
        tweetMetricsMapper.incrementReply(tweet.getTweetId());
        TweetMetrics metrics = tweetMetricsMapper.findByTweetId(tweet.getTweetId());
        assertThat(metrics.getReplyCount()).isEqualTo(1);

        tweetMetricsMapper.decrementReply(tweet.getTweetId());
        TweetMetrics metrics2 = tweetMetricsMapper.findByTweetId(tweet.getTweetId());
        assertThat(metrics2.getReplyCount()).isEqualTo(0);
    }
}