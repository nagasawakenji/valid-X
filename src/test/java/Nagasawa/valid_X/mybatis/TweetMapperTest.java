package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class TweetMapperTest {

    @Autowired
    TweetMapper tweetMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    PostMapper postMapper;

    Tweet tweet;
    User user;

    @BeforeEach
    void setup() {
        Instant fixedTime = Instant.parse("2025-10-13T12:00:00Z");

        user = User.builder()
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

    }

    @Test
    @DisplayName("正常系: 正しくポストを取得できる")
    void selectTweetById_success() {
        Tweet found = tweetMapper.selectTweetById(tweet.getTweetId());

        assertThat(found.getTweetId()).isEqualTo(tweet.getTweetId());
        assertThat(found.getUserId()).isEqualTo(user.getId());
        assertThat(found.getContent()).isEqualTo(tweet.getContent());
    }

}
