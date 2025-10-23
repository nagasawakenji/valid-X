package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Follow;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.FollowMapper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class FollowMapperTest {

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;

    User followerUser;
    User followeeUser;
    Follow follow;

    @BeforeEach
    void setup() {
        Instant fixedTime = Instant.parse("2025-10-13T12:00:00Z");
        followerUser = User.builder()
                .username("test_follower_user")
                .displayName("TestFollowerDisplay")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(followerUser);

        followeeUser = User.builder()
                .username("test_followee_user")
                .displayName("TestFolloweeDisplay")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(followeeUser);

        follow = Follow.builder()
                .followerId(followerUser.getId())
                .followeeId(followeeUser.getId())
                .build();
    }

    // insertのテストコード
    @Test
    @DisplayName("正常系: 正しくINSERTができる")
    void insert_success() {
        int inserted = followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        assertThat(inserted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 重複INSERTで冪等性を維持する")
    void insert_duplicate() {
        int insertedFirstTime = followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());
        int insertedSecondTime = followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        assertThat(insertedFirstTime).isEqualTo(1);
        assertThat(insertedSecondTime).isEqualTo(0);

    }

    @Test
    @DisplayName("異常系: 同一id指定(chk_follow_self違反)でRunTimeExceptionを返す")
    void insert_duplicate_runTimeException() {
        assertThatThrownBy(() -> followMapper.insert(follow.getFolloweeId(), follow.getFolloweeId()))
                .isInstanceOf(RuntimeException.class);
    }

    // deleteのテストコード
    @Test
    @DisplayName("正常系: 正しくDELETEが実行される")
    void delete_success() {
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        int deleted = followMapper.delete(follow.getFollowerId(), follow.getFolloweeId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: 存在しないfollow削除で冪等性を保持する")
    void delete_notFound_andReturn0() {
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        int deletedFirstTime = followMapper.delete(follow.getFollowerId(), follow.getFolloweeId());
        int deletedSecondTime = followMapper.delete(follow.getFollowerId(), follow.getFolloweeId());

        assertThat(deletedFirstTime).isEqualTo(1);
        assertThat(deletedSecondTime).isEqualTo(0);
    }

    // existのテストコード
    @Test
    @DisplayName("正常系: 存在するレコードに対してtrueを返す")
    void exist_success_true() {
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        assertThat(followMapper.exists(follow.getFollowerId(), follow.getFolloweeId())).isTrue();
    }

    @Test
    @DisplayName("正常系: 存在しないレコードに対してfalseを返す")
    void exist_success_false() {
        assertThat(followMapper.exists(follow.getFollowerId(), follow.getFolloweeId())).isFalse();
    }

    // countFollowersのテストコード
    @Test
    @DisplayName("正常系: 正しくフォロワー数を返す")
    void countFollowers_success() {
        // followeeIdのユーザーがfollowerIdのユーザーをフォローした
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        Long followersCount = followMapper.countFollowers(follow.getFolloweeId());
        assertThat(followersCount).isEqualTo(1);
    }

    // countFollowingのテストコード
    @Test
    @DisplayName("正常系: 正しくフォロー数を返す")
    void countFollowees_success() {
        // followeeIdのユーザーがfollowerIdのユーザーをフォローした
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        Long followersCount = followMapper.countFollowing(follow.getFollowerId());
        assertThat(followersCount).isEqualTo(1);
    }

    // listFollowersのテストコード
    @Test
    @DisplayName("正常系: 正しくフォロワーのidを返す")
    void listFollowers_success() {
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        List<Long> followerIds = followMapper.listFollowers(follow.getFolloweeId(), null, 10);
        assertThat(followerIds).isEqualTo(List.of(follow.getFollowerId()));
    }

    // listFollowingのテストコード
    @Test
    @DisplayName("正常系: 正しくフォローユーザーのidを返す")
    void listFollowing_success() {
        followMapper.insert(follow.getFollowerId(), follow.getFolloweeId());

        List<Long> followingIds = followMapper.listFollowing(follow.getFollowerId(), null, 10);
        assertThat(followingIds).isEqualTo(List.of(follow.getFolloweeId()));
    }

}
