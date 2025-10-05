package Nagasawa.valid_X.infra.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeleteMapper {

    int deleteTweetById(@Param("tweetId") Long tweetId);

    int deleteTweetMetricsById(@Param("tweetId") Long tweetId);

    int deleteLikeById(@Param("tweetId") Long tweetId);

    int deleteRepostById(@Param("tweetId") Long tweetId);

}
