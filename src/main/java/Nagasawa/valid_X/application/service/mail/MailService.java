package Nagasawa.valid_X.application.service.mail;

import java.time.Instant;

public interface MailService {
    void sendMagicLoginLink(String toEmail, String url, Instant expiresAt);
}
