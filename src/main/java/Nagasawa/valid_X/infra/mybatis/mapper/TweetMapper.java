package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.Tweet;
import org.apache.ibatis.annotations.Param;

public interface TweetMapper {

    Tweet selectTweetById(@Param("tweetId") Long tweetId);
}
