package Nagasawa.valid_X.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// マジックリンク発行時の返り値DTO
@Builder
@Getter
public class MagicLinkIssue {
    String url;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant expiresAt;
}
