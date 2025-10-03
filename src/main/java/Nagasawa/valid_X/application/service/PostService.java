package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.validation.TweetValidator;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final TweetValidator tweetValidator;
    private final TweetConverter tweetConverter;

    @Transactional
    public PostResult post(Long userId, PostForm postForm) {

        Tweet tweet = tweetConverter.toTweet(postForm, userId);
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

        // tweetMediaの作成
        List<TweetMedia> links = tweetConverter.linkTweetMedias(tweetId, mediaIds);
        for (TweetMedia tm: links) {
            postMapper.insertTweetMedia(tm);
        }

        PostResult postResult = tweetConverter.toPostResult(tweet, medias);

        return postResult;
    }


}
