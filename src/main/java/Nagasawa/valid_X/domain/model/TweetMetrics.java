package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TweetMetrics {
    private Long tweetId;
    private int likeCount;
    private int repostCount;
    private int replyCount;
}