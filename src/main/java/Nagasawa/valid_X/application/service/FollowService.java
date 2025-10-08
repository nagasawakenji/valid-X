package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.FollowStatusResult;
import Nagasawa.valid_X.domain.dto.UserSummary;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.FollowMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper; // ある前提（selectById用）
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper; // なければ target の存在確認ロジックを別途

    @Transactional
    public FollowStatusResult follow(Long viewerId, Long targetUserId) {
        if (viewerId.equals(targetUserId)) {
            throw new IllegalArgumentException("cannot follow yourself");
        }

        if (userMapper.findById(targetUserId) == null) {
            // userが存在しない
            throw new NotFoundProblemException("user not found: id=" + targetUserId);
        }

        try {
            int inserted = followMapper.insert(viewerId, targetUserId); // 1 or 0（冪等）
            boolean following = inserted == 1 || followMapper.exists(viewerId, targetUserId);
            long followerCount = followMapper.countFollowers(targetUserId);
            long followingCount = followMapper.countFollowing(viewerId);

            log.debug("follow: viewerId={}, targetUserId={}, inserted={}", viewerId, targetUserId, inserted);
            return FollowStatusResult.builder()
                    .targetUserId(targetUserId)
                    .following(following)
                    .followerCount(followerCount)
                    .followingCount(followingCount)
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundProblemException("user not found: id=" + targetUserId);
        }
    }

    @Transactional
    public FollowStatusResult unfollow(Long viewerId, Long targetUserId) {
        if (viewerId.equals(targetUserId)) {
            // 自分をunfollowは常に無意味（0で返るだけ）
            return FollowStatusResult.builder()
                    .targetUserId(targetUserId)
                    .following(false)
                    .followerCount(followMapper.countFollowers(targetUserId))
                    .followingCount(followMapper.countFollowing(viewerId))
                    .build();
        }

        if (userMapper.findById(targetUserId) == null) {
            // userが見つからない
            throw new NotFoundProblemException("user not found: id=" + targetUserId);
        }

        int deleted = followMapper.delete(viewerId, targetUserId); // 1 or 0（冪等）
        boolean following = deleted == 0 && followMapper.exists(viewerId, targetUserId); // 通常 false
        long followerCount = followMapper.countFollowers(targetUserId);
        long followingCount = followMapper.countFollowing(viewerId);

        log.debug("unfollow: viewerId={}, targetUserId={}, deleted={}", viewerId, targetUserId, deleted);
        return FollowStatusResult.builder()
                .targetUserId(targetUserId)
                .following(following)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserSummary> listFollowers(Long userId, Long cursorId, int limit) {
        List<Long> ids = followMapper.listFollowers(userId, cursorId, limit);

        return userMapper.findSummariesByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> listFollowing(Long userId, Long cursor, int limit) {
        List<Long> ids = followMapper.listFollowing(userId, cursor, limit);

        return userMapper.findSummariesByIds(ids);
    }
}