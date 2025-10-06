package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.Tweet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TweetMapper {

    Tweet selectTweetById(@Param("tweetId") Long tweetId);
}
