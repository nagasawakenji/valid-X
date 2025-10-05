package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.Tweet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReplyMapper {
    /** 親ツイートが存在するか */
    boolean parentExists(@Param("tweetId") Long tweetId);

    /** 親ツイートを取得（必要なら） */
    Tweet findTweet(@Param("tweetId") Long tweetId);

    /** 返信一覧（ページング） */
    List<Tweet> listReplies(
            @Param("parentTweetId") Long parentTweetId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /** 件数 */
    long countReplies(@Param("parentTweetId") Long parentTweetId);
}