package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Tweet {
    private Long tweetId;
    private Long userId;
    private String content;
    private Long inReplyToTweetId;
    private LocalDateTime createdAt;
}
