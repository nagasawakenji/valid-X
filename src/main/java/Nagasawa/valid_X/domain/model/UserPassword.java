package Nagasawa.valid_X.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPassword {
    private Long userId;
    private String passwordHash;
    private String algorithm;
    private int strength;
    private Instant passwordUpdatedAt;
    private boolean rehashRequired;
}
