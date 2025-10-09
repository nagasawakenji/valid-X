package Nagasawa.valid_X.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class GetPostResult {
    Long tweetId;
    Long userId;
    String username;
    String content;
    Long inReplyToTweetId;
    Instant createdAt;

    long likeCount;
    long repostCount;
    long replyCount;

    Boolean likedByMe;
    Boolean repostedByMe;

    List<GetMediaResult> media;
}
