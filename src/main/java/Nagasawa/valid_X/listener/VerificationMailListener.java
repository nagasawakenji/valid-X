package Nagasawa.valid_X.listener;

import Nagasawa.valid_X.event.VerificationMailRequestedEvent;
import Nagasawa.valid_X.service.VerificationService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class VerificationMailListener {
    private final VerificationService verificationService;

    public VerificationMailListener(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

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
