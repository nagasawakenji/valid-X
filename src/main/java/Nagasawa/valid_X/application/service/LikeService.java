package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.LikeStatusResult;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.LikeMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final TweetMetricsMapper tweetMetricsMapper;
    private final LikeMapper likeMapper;

    @Transactional
    public LikeStatusResult like(Long tweetId, Long userId) {
        try {
            // ON CONFLICT DO NOTHING のINSERT。1=新規追加、0=既にlike済み
            int inserted = likeMapper.insert(userId, tweetId);

            if (inserted == 1) {
                tweetMetricsMapper.incrementLike(tweetId); // +1
                log.debug("liked: tweetId={}, userId={}, inserted={}", tweetId, userId, inserted);
                long likeCount = tweetMetricsMapper.getLikeCount(tweetId); // 取れるなら返す
                return LikeStatusResult.of(tweetId, true, likeCount);
            } else {
                // 既にlike済み
                long likeCount = tweetMetricsMapper.getLikeCount(tweetId);
                return LikeStatusResult.of(tweetId, true, likeCount);
            }
        } catch (DataIntegrityViolationException e) {
            // ツイートが存在しない/権限なし 等を 404/403 相当の Problem に包む
            throw new NotFoundProblemException("tweet not found: id=" + tweetId);
        }
    }

    @Transactional
    public LikeStatusResult unlike(Long tweetId, Long userId) {
        try {
            // 1=解除、0=元々未like
            int deleted = likeMapper.delete(userId, tweetId);
            if (deleted == 1) {
                tweetMetricsMapper.decrementLike(tweetId);
                log.debug("unliked: tweetId={}, userId={}, deleted={}", tweetId, userId, deleted);
                long likeCount = tweetMetricsMapper.getLikeCount(tweetId);
                return LikeStatusResult.of(tweetId, false, likeCount);
            } else {
                long likeCount = tweetMetricsMapper.getLikeCount(tweetId);
                return LikeStatusResult.of(tweetId, false, likeCount);
            }
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundProblemException("tweet not found: id=" + tweetId);
        }
    }
}