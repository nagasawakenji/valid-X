package Nagasawa.valid_X.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Follow {
    private Long followerId;
    private Long followeeId;
    private LocalDateTime createdAt;
}
