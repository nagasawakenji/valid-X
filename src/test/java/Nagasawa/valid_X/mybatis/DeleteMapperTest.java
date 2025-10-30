package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.*;
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
public class DeleteMapperTest {
    @Autowired
    DeleteMapper deleteMapper;
    @Autowired
    PostMapper postMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    TweetMetricsMapper tweetMetricsMapper;
    @Autowired
    LikeMapper likeMapper;
    @Autowired
    RepostMapper repostMapper;

    User user;
    Tweet tweet;
    Instant fixedTime;

    @BeforeEach
    void setup() {
        fixedTime = Instant.parse("2025-10-13T12:00:00Z");
        user = user = User.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(user);

        tweet = Tweet.builder()
                .userId(user.getId())
                .content("hello test")
                .createdAt(fixedTime)
                .build();
        postMapper.insertTweet(tweet);

        // 自身のポストに対してもlikeは実行できる
        likeMapper.insert(user.getId(), tweet.getTweetId());
        repostMapper.insert(user.getId(), tweet.getTweetId());
        tweetMetricsMapper.insertInit(tweet.getTweetId());
    }

    // deleteTweetByIdのテスト
    @Test
    @DisplayName("正常系: id指定で正しくポストを削除できる")
    void deleteTweetById_success() {
        int deleted = deleteMapper.deleteTweetById(tweet.getTweetId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 存在しないidに対しては冪等性を維持する")
    void deleteTweetById_notFound() {
        int deletedFirstTime = deleteMapper.deleteTweetById(tweet.getTweetId());
        assertThat(deletedFirstTime).isEqualTo(1);
        int deletedSecondTime = deleteMapper.deleteTweetById(tweet.getTweetId());
        assertThat(deletedSecondTime).isEqualTo(0);
    }

    // deleteTweetMetricsByIdのテスト
    @Test
    @DisplayName("正常系: id指定で正しくメトリクスを削除できる")
    void deleteTweetMetricsById_success() {
        int deleted = deleteMapper.deleteTweetMetricsById(tweet.getTweetId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 存在しないidに対しては冪等性を維持する")
    void deleteTweetMetricsById_notFound() {
        int deletedFirstTime = deleteMapper.deleteTweetMetricsById(tweet.getTweetId());
        assertThat(deletedFirstTime).isEqualTo(1);
        int deletedSecondTime = deleteMapper.deleteTweetMetricsById(tweet.getTweetId());
        assertThat(deletedSecondTime).isEqualTo(0);
    }

    // deleteLikeByIdのテスト
    @Test
    @DisplayName("正常系: id指定でlikeを削除できる")
    void deleteLikeById_success() {
        int deleted = deleteMapper.deleteLikeById(tweet.getTweetId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 存在しないidに対しては冪等性を維持する")
    void deleteLikeById_notFound() {
        int deletedFirstTime = deleteMapper.deleteLikeById(tweet.getTweetId());
        assertThat(deletedFirstTime).isEqualTo(1);
        int deletedSecondTime = deleteMapper.deleteLikeById(tweet.getTweetId());
        assertThat(deletedSecondTime).isEqualTo(0);
    }

    // deleteRepostByIdのテスト
    @Test
    @DisplayName("正常系: id指定でrepostを削除できる")
    void deleteRepostById_success() {
        int deleted = deleteMapper.deleteRepostById(tweet.getTweetId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 存在しないidに対しては冪等性を維持する")
    void deleteRepostById_notFound() {
        int deletedFirstTime = deleteMapper.deleteRepostById(tweet.getTweetId());
        assertThat(deletedFirstTime).isEqualTo(1);
        int deletedSecondTime = deleteMapper.deleteRepostById(tweet.getTweetId());
        assertThat(deletedSecondTime).isEqualTo(0);
    }
}

