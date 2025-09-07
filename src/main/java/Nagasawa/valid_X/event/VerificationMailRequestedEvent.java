package Nagasawa.valid_X.event;


// pending_usersへのinsert完了後にverificationServiceへイベント通知をする
public record VerificationMailRequestedEvent(
        String email,
        String urlToken
) {}
