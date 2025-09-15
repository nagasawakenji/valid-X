package Nagasawa.valid_X.listener;

import Nagasawa.valid_X.event.VerificationMailRequestedEvent;
import Nagasawa.valid_X.application.service.VerificationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationMailListener {
    private final VerificationService verificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(VerificationMailRequestedEvent event) {
        try {
            verificationService.sendVerificationMail(event.email(), event.urlToken());
        } catch (MessagingException e) {
            // @TransactionalEventListenerはエラーを拾わないので、ログで流す
            log.error("Failed to send verification mail to {}", event.email());
        }
    }
}
