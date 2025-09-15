package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TweetMedia {
    private Long tweetId;
    private Long mediaId;
    private int position;
}
