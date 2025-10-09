package Nagasawa.valid_X.domain.dto;

import lombok.Value;

@Value
public class GetMediaResult {
    Long tweetId;
    Long mediaId;
    String type;
    String mimeType;
    Long bytes;
    Integer width;
    Integer height;
    Integer durationMs;
    String blurhash;
    String storageKey;
}
