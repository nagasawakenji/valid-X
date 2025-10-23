package Nagasawa.valid_X.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class Follow {
    private Long followerId;
    private Long followeeId;
    private Instant createdAt;
}
