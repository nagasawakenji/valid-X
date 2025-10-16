package Nagasawa.valid_X.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetMediaResult {
    Long tweetId;
    Long mediaId;
    String mediaType;
    String mimeType;
    Long bytes;
    Integer width;
    Integer height;
    Integer durationMs;
    String blurhash;
    String storageKey;
}
