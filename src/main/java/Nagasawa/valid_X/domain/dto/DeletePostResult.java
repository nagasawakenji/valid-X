package Nagasawa.valid_X.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeletePostResult {
    Long tweetId;
    int deletedTweet;
    int deletedLikes;
    int deletedReposts;
    int deletedMetrics;
}
