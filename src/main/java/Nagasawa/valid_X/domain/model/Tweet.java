package Nagasawa.valid_X.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Tweet {
    private Long tweetId;
    private Long userId;
    private String content;
    private Long inReplyToTweetId;
    private Instant createdAt;
}
