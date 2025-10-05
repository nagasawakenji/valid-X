package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.validation.TweetValidator;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.ReplyMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.TweetMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyService {

    private final ReplyMapper replyMapper;
    private final PostMapper postMapper;
    private final TweetValidator tweetValidator;
    private final TweetConverter tweetConverter;
    private final TweetMetricsMapper tweetMetricsMapper;

    @Transactional
    public PostResult reply(Long parentTweetId, Long userId, PostForm postForm) {

        if (!replyMapper.parentExists(parentTweetId)) {
            throw new IllegalArgumentException("parent tweet not found: id=" + parentTweetId);
        }

        Tweet tweet = tweetConverter.toTweet(postForm, userId);
        // postFormのinReplyToTweetを上書きする
        tweet.setInReplyToTweetId(parentTweetId);
        List<Media> medias = tweetConverter.toMedias(postForm);

        tweetValidator.validateContent(tweet.getContent());
        tweetValidator.validateMedia(medias);

        // tweetのINSERT
        postMapper.insertTweet(tweet);
        Long tweetId = tweet.getTweetId();

        // mediaのINSERT
        List<Long> mediaIds = new ArrayList<>(medias.size());
        for (Media m: medias) {
            postMapper.insertMedia(m);
            mediaIds.add(m.getMediaId());
        }

        // tweetMetricsのINSERT
        tweetMetricsMapper.insertInit(tweetId);

        // tweetMediaの作成
        List<TweetMedia> links = tweetConverter.linkTweetMedias(tweetId, mediaIds);
        for (TweetMedia tm: links) {
            postMapper.insertTweetMedia(tm);
        }

        // 親ポストの返信数を+1する
        tweetMetricsMapper.incrementReply(parentTweetId);

        PostResult postResult = tweetConverter.toPostResult(tweet, medias);

        return postResult;
    }


}
