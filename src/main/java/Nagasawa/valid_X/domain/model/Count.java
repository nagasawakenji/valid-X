package Nagasawa.valid_X.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class Count {
    private Long userId;
    private int followers;
    private int following;
    private int tweets;
    private Instant updatedAt;
}
