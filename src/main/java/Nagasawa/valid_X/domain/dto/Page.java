package Nagasawa.valid_X.domain.dto;

import lombok.Value;
import java.util.List;

@Value
public class Page<T> {
    List<T> items;
    Long nextCursor; // まだ続きがある場合は次に渡すcarosr(tweet_id)
}