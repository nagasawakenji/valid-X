package Nagasawa.valid_X.listener;

import Nagasawa.valid_X.application.service.OneTimeLoginTokenService;
import Nagasawa.valid_X.application.service.mail.SmtpMailService;
import Nagasawa.valid_X.event.MagicLoginLinkRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MagicLinkListener {
    private final OneTimeLoginTokenService oneTimeLoginTokenService;
    private final SmtpMailService smtpMailService;

    @Value("${app.frontend.base-url}")
    private String appBaseUrl;

    @Value("${app.magic-link.ttl-minutes:10}")
    private int ttlMinutes;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MagicLoginLinkRequestedEvent e) {
        var link = oneTimeLoginTokenService.issueLoginLink(e.userId(), Duration.ofMinutes(ttlMinutes), appBaseUrl);
        smtpMailService.sendMagicLoginLink(e.email(), link.getUrl(), link.getExpiresAt());
    }
}
