package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class User {
    private Long id;
    private String username;
    private String displayName;
    private String locale;
    private Instant createdAt;
    private String timezone;
}
