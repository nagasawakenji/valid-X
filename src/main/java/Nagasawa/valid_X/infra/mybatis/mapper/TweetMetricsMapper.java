package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.TweetMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TweetMetricsMapper {

    int getLikeCount(@Param("tweetId") Long tweetId);

    int getRepostCount(@Param("tweetId") Long tweetId);

    int insertInit(@Param("tweetId") Long tweetId);

    TweetMetrics findByTweetId(@Param("tweetId") Long tweetId);

    int incrementLike(@Param("tweetId") Long tweetId);

    int decrementLike(@Param("tweetId") Long tweetId);

    int incrementRepost(@Param("tweetId") Long tweetId);

    int decrementRepost(@Param("tweetId") Long tweetId);

    int incrementReply(@Param("tweetId") Long tweetId);

    int decrementReply(@Param("tweetId") Long tweetId);
}