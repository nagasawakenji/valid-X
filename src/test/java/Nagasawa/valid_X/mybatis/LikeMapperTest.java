package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.DeleteMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.LikeMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
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
public class LikeMapperTest {

    @Autowired
    private LikeMapper likeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private DeleteMapper deleteMapper;

    Instant fixedTime = Instant.parse("2025-10-13T12:00:00Z");
    User user;
    Tweet tweet;

    @BeforeEach
    void setup() {

        user = User.builder()
                .username("test_follower_user")
                .displayName("TestFollowerDisplay")
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

    // insertのテスト
    @Test
    @DisplayName("正常系: likeが正しく実行される")
    void insert_success() {
        int inserted = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: すでにlikeを実行していた場合、冪等性を保持する")
    void insert_already() {
        int insertedFirstTime = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(insertedFirstTime).isEqualTo(1);
        int insertedSecondTime = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(insertedSecondTime).isEqualTo(0);
    }

    // deleteのテスト
    @Test
    @DisplayName("正常系: 正しくlikeを削除できる")
    void delete_success() {
        int inserted = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(inserted).isEqualTo(1);
        int deleted = likeMapper.delete(user.getId(), tweet.getTweetId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: いいねをしていない場合、冪等性を維持する")
    void delete_already() {
        int inserted = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(inserted).isEqualTo(1);
        int deletedFirstTime = likeMapper.delete(user.getId(), tweet.getTweetId());
        assertThat(deletedFirstTime).isEqualTo(1);
        int deletedSecondTime = likeMapper.delete(user.getId(), tweet.getTweetId());
        assertThat(deletedSecondTime).isEqualTo(0);
    }

    // existのテストコード
    @Test
    @DisplayName("正常系: 正しく存在判定ができる")
    void exist_success() {
        int inserted = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(inserted).isEqualTo(1);
        boolean isExist = likeMapper.exists(user.getId(), tweet.getTweetId());
        assertThat(isExist).isTrue();
    }

    @Test
    @DisplayName("正常系: 存在しない場合はfalseを返す")
    void exist_notFound() {
        boolean isExist = likeMapper.exists(user.getId(), tweet.getTweetId());
        assertThat(isExist).isFalse();
    }

    // constraintに対するテスト
    @Test
    @DisplayName("正常系: ポストを削除すると、ON DELETE CASECADE で自動削除される")
    void constraint_fKey_tweetId() {
        int inserted = likeMapper.insert(user.getId(), tweet.getTweetId());
        assertThat(inserted).isEqualTo(1);
        int deleted = deleteMapper.deleteTweetById(tweet.getTweetId());
        assertThat(deleted).isEqualTo(1);

        boolean isExist = likeMapper.exists(user.getId(), tweet.getTweetId());
        assertThat(isExist).isFalse();
    }

}
