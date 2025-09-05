package Nagasawa.valid_X.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class User {
    private Long id;
    private String username;
    private String displayName;
    private LocalDateTime createdAt;
}
