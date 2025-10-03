package Nagasawa.valid_X.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MediaResult {
    Long mediaId;
    String mediaType;
    String mimeType;
    Long bytes;
    int width;
    int height;
    int durationMs;
    String blurhash;
    String storageKey;
    Instant createdAt;
}