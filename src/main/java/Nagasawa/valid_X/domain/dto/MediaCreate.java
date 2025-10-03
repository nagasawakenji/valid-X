package Nagasawa.valid_X.domain.dto;

public record MediaCreate(
        String mediaType,         // "image" / "video" など
        String mimeType,
        Long bytes,
        int width,
        int height,
        int durationMs,
        byte[] sha256,            // ← `Media` は byte じゃなく byte[] 推奨
        String blurhash,
        String storageKey
) {}
