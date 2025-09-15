package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserEmail {
    private Long userId;
    private String email;
    private Instant createdAt;
}
