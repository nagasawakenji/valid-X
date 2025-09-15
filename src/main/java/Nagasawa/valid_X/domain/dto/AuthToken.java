package Nagasawa.valid_X.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AuthToken {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String token;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiresAt;
}
