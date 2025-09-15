package Nagasawa.valid_X.event;

import java.time.Instant;

// usersなどへのINSERTが完了したことをLoginServiceへ通知する
public record GenerateAuthTokenRequestEvent(
        Long userId,
        Instant now
){}
