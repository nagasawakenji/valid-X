package Nagasawa.valid_X.event;

import java.time.Instant;

public record MagicLoginLinkRequestedEvent(Long userId, String email, Instant at) {
}
