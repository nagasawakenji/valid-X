package Nagasawa.valid_X.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class PostResult {
    Long tweetId;
    Long userId;
    String content;
    Long inReplyToTweetId;
    Instant createdAt;
    List<Nagasawa.valid_X.domain.dto.MediaResult> medias;
}