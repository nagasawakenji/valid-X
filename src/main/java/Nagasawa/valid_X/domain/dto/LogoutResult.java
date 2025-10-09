package Nagasawa.valid_X.domain.dto;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class LogoutResult {
    Long userId;
    UUID sessionUuid;
    boolean alreadyRevoked;
    Instant revokedAt;
    int refreshRevoked;
    public static LogoutResult of(Long userId, UUID sessionId,
                                  boolean alreadyRevoked, Instant revokedAt,
                                  int refreshRevoked) {
        return new LogoutResult(userId, sessionId, alreadyRevoked, revokedAt, refreshRevoked);
    }
}