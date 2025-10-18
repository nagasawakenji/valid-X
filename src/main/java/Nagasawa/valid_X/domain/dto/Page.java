package Nagasawa.valid_X.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class Page<T> {
    List<T> items;
    Long nextCursor;
    Long nextCursorLike;
}