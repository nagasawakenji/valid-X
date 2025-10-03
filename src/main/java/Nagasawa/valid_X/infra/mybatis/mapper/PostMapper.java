package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper {

    int insertTweet(Tweet tweet);

    int insertTweetMedia(TweetMedia tweetMedia);

    int insertMedia(Media media);
}
