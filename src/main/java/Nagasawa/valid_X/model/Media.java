package Nagasawa.valid_X.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Media {
    private Long mediaId;
    private String mediaType;
    private String mimeType;
    private Long bytes;
    private int width;
    private int height;
    private int durationMs;
    private byte sha256;
    private String blurhash;
    private String storageKey;
    private LocalDateTime createdAt;

}
