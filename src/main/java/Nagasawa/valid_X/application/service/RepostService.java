package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.RepostStatusResult;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.RepostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepostService {

    private final RepostMapper repostMapper;
    private final TweetMetricsMapper tweetMetricsMapper;

    /** リポスト（冪等） */
    @Transactional
    public RepostStatusResult repost(Long tweetId, Long userId) {
        try {
            int inserted = repostMapper.insert(userId, tweetId); // ON CONFLICT DO NOTHING 前提
            if (inserted == 1) {
                tweetMetricsMapper.incrementRepost(tweetId);
                log.debug("reposted: tweetId={}, userId={}", tweetId, userId);
            } else {
                log.debug("already reposted: tweetId={}, userId={}", tweetId, userId);
            }
            long count = tweetMetricsMapper.getRepostCount(tweetId);
            return RepostStatusResult.of(tweetId, true, count);
        } catch (DataIntegrityViolationException e) {
            // たとえば元ツイが存在しない（FK違反）など
            throw new NotFoundProblemException("tweet not found: id=" + tweetId);
        }
    }

    /** リポスト解除（冪等） */
    @Transactional
    public RepostStatusResult unrepost(Long tweetId, Long userId) {
        try {
            int deleted = repostMapper.delete(userId, tweetId);
            if (deleted == 1) {
                tweetMetricsMapper.decrementRepost(tweetId);
                log.debug("unreposted: tweetId={}, userId={}", tweetId, userId);
            } else {
                log.debug("already unreposted: tweetId={}, userId={}", tweetId, userId);
            }
            long count = tweetMetricsMapper.getRepostCount(tweetId);
            return RepostStatusResult.of(tweetId, false, count);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundProblemException("tweet not found: id=" + tweetId);
        }
    }
}