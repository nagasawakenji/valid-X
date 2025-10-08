package Nagasawa.valid_X.application.service;

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

    private static <T extends TweetView> Page<T> toPage(List<T> rows, int limit) {
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
    public Page<TweetView> homeTimeline(Long viewerId, Long cursor, int limit) {
        List<TweetView> rows = timelineMapper.listHomeTimeline(viewerId, cursor, limit);
        return toPage(rows, limit);
    }

    @Transactional(readOnly = true)
    public Page<TweetView> replies(Long parentTweetId, Long cursor, int limit, Long viewerId) {
        List<TweetView> rows = timelineMapper.listReplies(parentTweetId, cursor, limit, viewerId);
        return toPage(rows, limit);
    }

    @Transactional(readOnly = true)
    public Page<TweetView> userTweets(Long targetUserId, Long cursor, int limit, Long viewerId) {
        List<TweetView> rows = timelineMapper.listUserTweets(targetUserId, cursor, limit, viewerId);
        return toPage(rows, limit);
    }

    @Transactional(readOnly = true)
    public Page<TweetView> popularTweets(Long viewerId, Long cursorLike, Long cursorId, int limit, int dayCount) {
        List<TweetView> rows = timelineMapper.listPopularTweets(viewerId, cursorLike, cursorId, limit, dayCount);
        return toPage(rows, limit);
    }
}