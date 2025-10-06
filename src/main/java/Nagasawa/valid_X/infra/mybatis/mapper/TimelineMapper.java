// infra/mybatis/mapper/TimelineMapper.java
package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.dto.TweetView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimelineMapper {

    // ホームTL（自分 + 自分がフォローしているユーザのツイート）
    List<TweetView> listHomeTimeline(
            @Param("viewerId") Long viewerId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit);

    // あるツイートへの返信一覧
    List<TweetView> listReplies(
            @Param("parentTweetId") Long parentTweetId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit,
            @Param("viewerId") Long viewerId);

    // 特定ユーザーのツイート一覧
    List<TweetView> listUserTweets(
            @Param("targetUserId") Long targetUserId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit,
            @Param("viewerId") Long viewerId);
}