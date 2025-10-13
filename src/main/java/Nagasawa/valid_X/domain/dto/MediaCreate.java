package Nagasawa.valid_X.domain.dto;

public record MediaCreate(
        String dataUrl,     // ← 必須: "data:image/png;base64,...."
        String mimeType,    // 任意: file.type があれば
        Integer width,      // 任意: 取得できれば（無ければサーバで算出 or null）
        Integer height,     // 任意
        Integer durationMs  // 任意（動画ならあれば嬉しい。無ければサーバで）
) {}
