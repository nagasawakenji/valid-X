package Nagasawa.valid_X.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Count {
    private Long userId;
    private int followers;
    private int following;
    private int tweets;
    private LocalDateTime updatedAt;
}
