package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.RepostMapper;
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
public class RepostMapperTest {

    @Autowired
    private RepostMapper repostMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;

    User user;
    Tweet tweet;
    Long userId;
    Long tweetId;

    @BeforeEach
    void setup() {
        Instant now = Instant.parse("2025-10-13T12:00:00Z");

        user = User.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .createdAt(now)
                .build();
        userMapper.insertUser(user);
        userId = user.getId();

        tweet = Tweet.builder()
                .userId(user.getId())
                .content("hello test!")
                .build();
        postMapper.insertTweet(tweet);

        tweetId = tweet.getTweetId();
    }

    @Test
    @DisplayName("正常系: 正しくinsertができる")
    void insert_success() {
        int inserted = repostMapper.insert(userId, tweetId);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 正しくdeleteができる")
    void delete_success() {
        int inserted = repostMapper.insert(userId, tweetId);
        assertThat(inserted).isEqualTo(1);

        int deleted = repostMapper.delete(userId, tweetId);
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 正しくexistで存在判定ができる")
    void exist_success() {
        int inserted = repostMapper.insert(userId, tweetId);
        assertThat(inserted).isEqualTo(1);

        boolean isExist = repostMapper.exists(userId, tweetId);
        assertThat(isExist).isTrue();
    }

    @Test
    @DisplayName("正常系: 存在しないレコードに対してexistはfalseを返す")
    void exist_notFound() {
        boolean isExist = repostMapper.exists(userId, tweetId);
        assertThat(isExist).isFalse();
    }
}
