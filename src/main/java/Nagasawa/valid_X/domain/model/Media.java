package Nagasawa.valid_X.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Media {
    private Long mediaId;
    private String mediaType;
    private String mimeType;
    private Long bytes;
    private Integer width;
    private Integer height;
    private Integer durationMs;
    private byte[] sha256;
    private String blurhash;
    private String storageKey;
    private Instant createdAt;

}
