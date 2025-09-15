package Nagasawa.valid_X.listener;

import Nagasawa.valid_X.application.service.LoginService;
import Nagasawa.valid_X.event.GenerateAuthTokenRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateAuthTokenListener {
    private final LoginService loginService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on (GenerateAuthTokenRequestEvent event) {
        loginService.generateAuthToken(event.userId(), event.now());
    }
}
