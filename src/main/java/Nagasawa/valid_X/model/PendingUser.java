package Nagasawa.valid_X.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PendingUser {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private boolean verified;
    private int resendCount;
    private LocalDateTime lastSentAt;
    private String locale;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
