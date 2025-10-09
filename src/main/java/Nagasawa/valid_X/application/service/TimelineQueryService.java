package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.TweetConverter;
import Nagasawa.valid_X.domain.dto.GetMediaResult;
import Nagasawa.valid_X.domain.dto.GetPostResult;
import Nagasawa.valid_X.domain.dto.Page;
import Nagasawa.valid_X.domain.dto.TweetView;
import Nagasawa.valid_X.infra.mybatis.mapper.TimelineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineQueryService {

    private final TimelineMapper timelineMapper;
    private final TweetConverter tweetConverter;

    private static <T extends GetPostResult> Page<T> toPage(List<T> rows, int limit) {
        boolean hasNext = rows.size() > limit;
        List<T> pageItems = hasNext ? rows.subList(0, limit) : rows;

        // 次カーソルは「返した最後のツイートのID」
        Long nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            T lastReturned = pageItems.get(pageItems.size() - 1);
            nextCursor = lastReturned.getTweetId();
        }

        // 次のpost取得でnextCursor未満のものが取得される
        return new Page<>(pageItems, nextCursor);
    }

    @Transactional(readOnly = true)
    public Page<GetPostResult> homeTimeline(Long viewerId, Long cursor, int limit) {
        List<TweetView> tweetViews = timelineMapper.listHomeTimeline(viewerId, cursor, limit);
        List<GetPostResult> getPostResultsRaw = tweetConverter.toGetPostResults(tweetViews);
        List<Long> tweetIds = getPostResultsRaw.stream()
                .map(GetPostResult::getTweetId)
                .toList();
        List<GetMediaResult> getMediaResults = timelineMapper.listMediaForTweetIds(tweetIds);
        List<GetPostResult> getPostResults = tweetConverter.mergePostsWithMedia(getPostResultsRaw, getMediaResults);

        return toPage(getPostResults, limit);

    }

    @Transactional(readOnly = true)
    public Page<GetPostResult> replies(Long parentTweetId, Long cursor, int limit, Long viewerId) {
        List<TweetView> tweetViews = timelineMapper.listReplies(parentTweetId, cursor, limit, viewerId);
        List<GetPostResult> getPostResultsRaw = tweetConverter.toGetPostResults(tweetViews);
        List<Long> tweetIds = getPostResultsRaw.stream()
                .map(GetPostResult::getTweetId)
                .toList();
        List<GetMediaResult> getMediaResults = timelineMapper.listMediaForTweetIds(tweetIds);
        List<GetPostResult> getPostResults = tweetConverter.mergePostsWithMedia(getPostResultsRaw, getMediaResults);

        return toPage(getPostResults, limit);
    }

    @Transactional(readOnly = true)
    public Page<GetPostResult> userTweets(Long targetUserId, Long cursor, int limit, Long viewerId) {
        List<TweetView> tweetViews = timelineMapper.listUserTweets(targetUserId, cursor, limit, viewerId);
        List<GetPostResult> getPostResultsRaw = tweetConverter.toGetPostResults(tweetViews);
        List<Long> tweetIds = getPostResultsRaw.stream()
                .map(GetPostResult::getTweetId)
                .toList();
        List<GetMediaResult> getMediaResults = timelineMapper.listMediaForTweetIds(tweetIds);
        List<GetPostResult> getPostResults = tweetConverter.mergePostsWithMedia(getPostResultsRaw, getMediaResults);

        return toPage(getPostResults, limit);
    }

    @Transactional(readOnly = true)
    public Page<GetPostResult> popularTweets(Long viewerId, Long cursorLike, Long cursorId, int limit, int dayCount) {
        List<TweetView> tweetViews = timelineMapper.listPopularTweets(viewerId, cursorLike, cursorId, limit, dayCount);
        List<GetPostResult> getPostResultsRaw = tweetConverter.toGetPostResults(tweetViews);
        List<Long> tweetIds = getPostResultsRaw.stream()
                .map(GetPostResult::getTweetId)
                .toList();
        List<GetMediaResult> getMediaResults = timelineMapper.listMediaForTweetIds(tweetIds);
        List<GetPostResult> getPostResults = tweetConverter.mergePostsWithMedia(getPostResultsRaw, getMediaResults);

        return toPage(getPostResults, limit);
    }
}