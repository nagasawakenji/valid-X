package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.*;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User user;
    private UserEmail userEmail;
    private Profile profile;
    private Count count;
    private Tweet tweet;
    private Media media;
    private TweetMedia tweetMedia;

    @BeforeEach
    void setup() {
        Instant now = Instant.parse("2025-10-13T12:00:00Z");
        user = User.builder()
                .id(1L)
                .username("test_user")
                .displayName("TestDisplay")
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .createdAt(now)
                .build();
        userMapper.insertUser(user);

        userEmail = UserEmail.builder()
                .userId(user.getId())
                .email("test@example.com")
                .createdAt(now)
                .build();
        userMapper.insertUserEmail(userEmail);

        profile = Profile.builder()
                .userId(user.getId())
                .bio("This is a test user for integration tests.")
                .avatarUrl("https://example.com/avatar/testuser.png")
                .protected_(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMapper.insertProfile(profile);

        count = Count.builder()
                .userId(user.getId())
                .followers(10)
                .following(5)
                .tweets(3)
                .updatedAt(now)
                .build();
        userMapper.insertCount(count);
    }

    @AfterEach
    void reset() {
        userMapper.deleteUser(user);
        userMapper.deleteUserEmail(user.getId());
        userMapper.deleteProfile(user.getId());
        userMapper.deleteCount(user.getId());
    }

    @Test
    @DisplayName("正常系: idで検索ができる")
    void findById_success() {
        User found = userMapper.findById(user.getId());
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
        assertThat(found.getDisplayName()).isEqualTo(user.getDisplayName());
        assertThat(found.getCreatedAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    @DisplayName("正常系: Usernameで検索ができる")
    void findByUsername_success() {
        User found = userMapper.findByUsername(user.getUsername());
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
        assertThat(found.getDisplayName()).isEqualTo(user.getDisplayName());
        assertThat(found.getCreatedAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    @DisplayName("正常系: emailでidが取得できる")
    void findByEmail() {
        Long foundId = userMapper.findByEmail(userEmail.getEmail());
        assertThat(foundId).isEqualTo(userEmail.getUserId());
    }

    @Test
    @DisplayName("正常系: emailで存在確認ができる")
    void existByEmail_success() {
        boolean isFound = userMapper.existsByEmail(userEmail.getEmail());
        assertThat(isFound).isTrue();
    }

    @Test
    @DisplayName("正常系: usernameで存在確認ができる")
    void existByUsername_success() {
        boolean isFound = userMapper.existsByUsername(user.getUsername());
        assertThat(isFound).isTrue();
    }

    @Test
    @DisplayName("正常系: userが正しく更新できる")
    void updateUser_success() {
        user.setDisplayName("UpdatedTestDisplay");
        userMapper.updateUser(user);
        User updatedUser = userMapper.findById(user.getId());
        assertThat(updatedUser.getDisplayName()).isEqualTo("UpdatedTestDisplay");
    }

    @Test
    @DisplayName("正常系: userが正しく削除できる")
    void deleteUser_success() {
        int deleted = userMapper.deleteUser(user);
        assertThat(deleted).isEqualTo(1);
        assertThat(userMapper.findById(user.getId())).isNull();
    }

    @Test
    @DisplayName("正常系: profileが正しく削除できる")
    void deleteProfile_success() {
        // 1. プロファイル削除前に再挿入は失敗する（userIdはPKなのでinsertで例外になるはず）ので、削除件数で判定
        int deleted = userMapper.deleteProfile(user.getId());
        assertThat(deleted).isEqualTo(1);
        // 2回目は既に削除済みなので0件
        int deletedAgain = userMapper.deleteProfile(user.getId());
        assertThat(deletedAgain).isEqualTo(0);
    }

    @Test
    @DisplayName("正常系: countが正しく削除できる")
    void deleteCount_success() {
        int deleted = userMapper.deleteCount(user.getId());
        assertThat(deleted).isEqualTo(1);
        int deletedAgain = userMapper.deleteCount(user.getId());
        assertThat(deletedAgain).isEqualTo(0);
    }
}
