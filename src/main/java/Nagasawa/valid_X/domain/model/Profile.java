package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Profile {
    private Long userId;
    private String bio;
    private String avatarUrl;
    private boolean protected_;
    private Instant createdAt;
    private Instant updatedAt;
}
