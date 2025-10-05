package Nagasawa.valid_X.infra.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RepostMapper {
    int insert(@Param("userId") Long userId, @Param("tweetId") Long tweetId);
    int delete(@Param("userId") Long userId, @Param("tweetId") Long tweetId);
    boolean exists(@Param("userId") Long userId, @Param("tweetId") Long tweetId);
}