package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.FollowService;
import Nagasawa.valid_X.domain.dto.FollowStatusResult;
import Nagasawa.valid_X.domain.dto.UserSummary;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.FollowMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class FollowServiceTest {

    @Mock
    private FollowMapper followMapper;
    @Mock
    private UserMapper userMapper;

    private FollowService followService;

    @BeforeEach
    void setup() {
        followService = new FollowService(
                followMapper,
                userMapper
        );
    }

    // follow() のテストコード
    @Test
    @DisplayName("正常系: 未フォローユーザーに対して、正しくfollow()が実行される")
    void follow_success_andCorrectEachField() {
        Long viewerId = 5L;
        Long targetId = 8L;

        // null回避のためのオブジェクトとして用いる
        User targetUser = User.builder()
                        .id(targetId)
                        .build();

        when(userMapper.findById(targetId)).thenReturn(targetUser);
        when(followMapper.insert(viewerId, targetId)).thenReturn(1);
        when(followMapper.countFollowers(targetId)).thenReturn(4L);
        when(followMapper.countFollowing(viewerId)).thenReturn(7L);

        // 実行
        FollowStatusResult result = followService.follow(viewerId, targetId);

        // resultの検証
        assertThat(result.getFollowerCount()).isEqualTo(4L);
        assertThat(result.getFollowingCount()).isEqualTo(7L);
        assertThat(result.isFollowing()).isTrue();

        // 各mapperの検証
        verify(followMapper).insert(viewerId, targetId);
        verify(followMapper).countFollowers(any());
        verify(followMapper).countFollowing(any());
    }

    @Test
    @DisplayName("正常系: すでにフォロー済みの場合、冪等性を保持する")
    void follow_alreadyFollowed() {
        Long viewerId = 5L;
        Long targetId = 8L;

        // null回避のためのオブジェクトとして用いる
        User targetUser = User.builder()
                .id(targetId)
                .build();

        when(userMapper.findById(targetId)).thenReturn(targetUser);
        when(followMapper.insert(viewerId, targetId)).thenReturn(0);
        when(followMapper.exists(viewerId, targetId)).thenReturn(true);
        when(followMapper.countFollowers(targetId)).thenReturn(4L);
        when(followMapper.countFollowing(viewerId)).thenReturn(7L);

        // 実行
        FollowStatusResult result = followService.follow(viewerId, targetId);

        // resultの検証
        assertThat(result.getFollowerCount()).isEqualTo(4L);
        assertThat(result.getFollowingCount()).isEqualTo(7L);
        assertThat(result.isFollowing()).isTrue();

        // 各mapperの検証
        verify(followMapper).insert(viewerId, targetId);
        verify(followMapper).countFollowers(any());
        verify(followMapper).countFollowing(any());
    }

    @Test
    @DisplayName("異常系: 自身をフォローしようとした場合、IllegalArgumentExceptionがスローされる")
    void follow_followOwn_illegalArgumentException() {
        Long viewerId = 5L;
        Long targetId = 5L;

        // エラーの検証
        assertThatThrownBy(() -> followService.follow(viewerId, targetId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cannot follow yourself");

        // DB操作は行われないことの検証
        verify(userMapper, never()).findById(targetId);
        verify(followMapper, never()).insert(viewerId, targetId);
        verify(followMapper, never()).countFollowers(any());
        verify(followMapper, never()).countFollowing(any());
    }

    @Test
    @DisplayName("異常系: フォロー対象ユーザーが存在しない場合、NotFoundProblemExceptionがスローされる")
    void follow_followUnexistUser_NotFoundException() {
        Long viewerId = 5L;
        Long targetId = 8L;

        when(userMapper.findById(targetId)).thenReturn(null);

        // エラーの検証
        assertThatThrownBy(() -> followService.follow(viewerId, targetId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("user not found: id=" + targetId);

        // followMapperは使用されないことの検証
        verify(followMapper, never()).countFollowing(any());
        verify(followMapper, never()).countFollowers(any());
    }

    // unfollow() のテストコード

    @Test
    @DisplayName("正常系: フォロー中のユーザーに対して、正しくunfollow()が実行される (新規削除)")
    void unfollow_success_newDelete() {
        Long viewerId = 5L;
        Long targetId = 8L;

        // null回避のためのオブジェクトとして用いる
        User targetUser = User.builder()
                .id(targetId)
                .build();

        when(userMapper.findById(targetId)).thenReturn(targetUser);
        when(followMapper.delete(viewerId, targetId)).thenReturn(1);
        when(followMapper.countFollowers(targetId)).thenReturn(4L);
        when(followMapper.countFollowing(viewerId)).thenReturn(7L);

        // 実行
        FollowStatusResult result = followService.unfollow(viewerId, targetId);

        // resultの検証
        assertThat(result.isFollowing()).isFalse();
        assertThat(result.getFollowerCount()).isEqualTo(4L);
        assertThat(result.getFollowingCount()).isEqualTo(7L);

        // Verify: deleteが呼ばれる
        verify(followMapper).delete(viewerId, targetId);
        verify(followMapper, never()).exists(anyLong(), anyLong()); // deleted == 1 の場合、existsは呼ばれない
    }

    @Test
    @DisplayName("正常系: すでにアンフォロー済みの場合、冪等性を保持する (delete=0)")
    void unfollow_alreadyUnfollowed_idempotent() {
        Long viewerId = 5L;
        Long targetId = 8L;

        // null回避のためのオブジェクトとして用いる
        User targetUser = User.builder()
                .id(targetId)
                .build();

        when(userMapper.findById(targetId)).thenReturn(targetUser);
        when(followMapper.delete(viewerId, targetId)).thenReturn(0);
        when(followMapper.exists(viewerId, targetId)).thenReturn(false);
        when(followMapper.countFollowers(targetId)).thenReturn(4L);
        when(followMapper.countFollowing(viewerId)).thenReturn(7L);


        // 実行
        FollowStatusResult result = followService.unfollow(viewerId, targetId);

        // 検証
        assertThat(result.isFollowing()).isFalse();
        assertThat(result.getFollowerCount()).isEqualTo(4L);
        assertThat(result.getFollowingCount()).isEqualTo(7L);

        // Verify: deleteとexistsの両方が呼ばれる
        verify(followMapper).delete(viewerId, targetId);
        verify(followMapper).exists(viewerId, targetId);
    }


    @Test
    @DisplayName("異常系: 自分自身をアンフォローしようとした場合、例外なく処理されfollowing=falseが返る")
    void unfollow_selfUnfollow_returnsFalse() {
        Long viewerId = 5L;
        Long targetId = 5L;
        when(followMapper.countFollowers(targetId)).thenReturn(5L); // 別のカウントを設定

        // 実行
        FollowStatusResult result = followService.unfollow(viewerId, targetId);

        // 検証
        assertThat(result.isFollowing()).isFalse();

        // Verify: deleteやuserMapper.findByIdは呼ばれず、count系だけが呼ばれる
        verify(userMapper, never()).findById(anyLong());
        verify(followMapper, never()).delete(anyLong(), anyLong());
        verify(followMapper).countFollowers(targetId);
        verify(followMapper).countFollowing(viewerId);
    }

    @Test
    @DisplayName("異常系: アンフォロー対象ユーザーがDBに存在しない場合、NotFoundProblemExceptionがスローされる")
    void unfollow_unexistUser_throwsNotFoundException() {
        Long viewerId = 5L;
        Long targetId = 8L;

        when(userMapper.findById(targetId)).thenReturn(null);

        // Act/Assert
        assertThatThrownBy(() -> followService.unfollow(viewerId, targetId))
                .isInstanceOf(NotFoundProblemException.class)
                .hasMessage("user not found: id=" + targetId);

        // Verify: deleteは呼ばれない
        verify(followMapper, never()).delete(anyLong(), anyLong());
    }

    // listFollowers() のテストコード
    @Test
    @DisplayName("正常系: listFollowers() が follower のIDを取得し、UserSummaryリストを返す")
    void listFollowers_success() {
        Long userId = 10L;
        Long cursorId = null;
        int limit = 3;

        List<Long> followerIds = List.of(1L, 2L, 3L);
        List<UserSummary> summaries = List.of(
                new UserSummary(1L, "alice", "Alice", "ja-JP", "Asia/Tokyo", "alice.png"),
                new UserSummary(2L, "bob", "Bob", "en-US", "America/New_York", "bob.png"),
                new UserSummary(3L, "carol", "Carol", "en-GB", "Europe/London", "carol.png")
        );

        when(followMapper.listFollowers(userId, cursorId, limit)).thenReturn(followerIds);
        when(userMapper.findSummariesByIds(followerIds)).thenReturn(summaries);

        List<UserSummary> result = followService.listFollowers(userId, cursorId, limit);

        // 検証
        verify(followMapper).listFollowers(userId, cursorId, limit);
        verify(userMapper).findSummariesByIds(followerIds);
        assertThat(result).containsExactlyElementsOf(summaries);
    }

    @Test
    @DisplayName("正常系: followerが空の場合、空のリストを返す")
    void listFollowers_empty() {
        Long userId = 10L;
        Long cursorId = null;
        int limit = 5;

        when(followMapper.listFollowers(userId, cursorId, limit)).thenReturn(List.of());
        when(userMapper.findSummariesByIds(List.of())).thenReturn(List.of());

        List<UserSummary> result = followService.listFollowers(userId, cursorId, limit);

        verify(followMapper).listFollowers(userId, cursorId, limit);
        verify(userMapper).findSummariesByIds(List.of());
        assertThat(result).isEmpty();
    }

    // listFollowing() のテストコード
    @Test
    @DisplayName("正常系: listFollowing() が following のIDを取得し、UserSummaryリストを返す")
    void listFollowing_success() {
        Long userId = 5L;
        Long cursor = 20L;
        int limit = 2;

        List<Long> followingIds = List.of(100L, 200L);
        List<UserSummary> summaries = List.of(
                new UserSummary(100L, "dave", "Dave", "ja-JP", "Asia/Tokyo", "dave.png"),
                new UserSummary(200L, "eve", "Eve", "en-US", "America/Los_Angeles", "eve.png")
        );

        when(followMapper.listFollowing(userId, cursor, limit)).thenReturn(followingIds);
        when(userMapper.findSummariesByIds(followingIds)).thenReturn(summaries);

        List<UserSummary> result = followService.listFollowing(userId, cursor, limit);

        verify(followMapper).listFollowing(userId, cursor, limit);
        verify(userMapper).findSummariesByIds(followingIds);
        assertThat(result).containsExactlyElementsOf(summaries);
    }

    @Test
    @DisplayName("正常系: followingが空の場合、空のリストを返す")
    void listFollowing_empty() {
        Long userId = 5L;
        Long cursor = null;
        int limit = 10;

        when(followMapper.listFollowing(userId, cursor, limit)).thenReturn(List.of());
        when(userMapper.findSummariesByIds(List.of())).thenReturn(List.of());

        List<UserSummary> result = followService.listFollowing(userId, cursor, limit);

        verify(followMapper).listFollowing(userId, cursor, limit);
        verify(userMapper).findSummariesByIds(List.of());
        assertThat(result).isEmpty();
    }




}
