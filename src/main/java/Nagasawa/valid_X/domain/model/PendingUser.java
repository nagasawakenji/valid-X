package Nagasawa.valid_X.domain.model;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingUser {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String passwordHash;
    private String tokenHash;
    private Instant expiresAt;
    private boolean verified;
    private int resendCount;
    private Instant lastSentAt;
    private String locale;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
