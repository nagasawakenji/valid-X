package Nagasawa.valid_X.application.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@example.com}")
    private String from;

    @Override
    public void sendMagicLoginLink(String toEmail, String url, Instant expiresAt) {
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.of("UTC"));
        String body = """
            ログイン用のリンクです。
            有効期限: %s

            %s

            ※ このリンクは一度しか使えません。心当たりがない場合は破棄してください。
            """.formatted(fmt.format(expiresAt), url);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("ログイン用マジックリンク");
        msg.setText(body);
        mailSender.send(msg);
    }
}
