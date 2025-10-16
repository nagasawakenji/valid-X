package Nagasawa.valid_X.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Tweet {
    private Long tweetId;
    private Long userId;
    private String content;
    private Long inReplyToTweetId;
    private Instant createdAt;
}
