package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.DeletePostResult;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.DeleteMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletePostService {

    private final TweetMapper tweetMapper;
    private final DeleteMapper deleteMapper;


    @Transactional
    public DeletePostResult delete(Long tweetId, Long userId) {

        Tweet tweet = tweetMapper.selectTweetById(tweetId);

        if (tweet == null) {
            // postが存在しない
            throw new NotFoundProblemException("post is not found tweetId: " + tweetId);
        }

        if (!tweet.getUserId().equals(userId)) {
            // userの不一致
            throw new IllegalArgumentException("user not matched userId: " + userId);
        }

        int deletedTweet = deleteMapper.deleteTweetById(tweetId);
        int deletedLikes = deleteMapper.deleteLikeById(tweetId);
        int deletedReposts = deleteMapper.deleteRepostById(tweetId);
        int deletedMetrics = deleteMapper.deleteTweetMetricsById(tweetId);

        DeletePostResult result = DeletePostResult.builder()
                .deletedTweet(deletedTweet)
                .deletedLikes(deletedLikes)
                .deletedReposts(deletedReposts)
                .deletedMetrics(deletedMetrics)
                .build();

        return result;
    }

}
