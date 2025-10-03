package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class Tweet {
    private Long tweetId;
    private Long userId;
    private String content;
    private Long inReplyToTweetId;
    private Instant createdAt;
}
