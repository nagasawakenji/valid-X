package Nagasawa.valid_X.service;

import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;

@Service
public class VerificationService {
    // 認証に用いるハッシュトークンなどに関するロジックを取りまとめる

    private final JavaMailSender javaMailSender;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public VerificationService(JavaMailSender javaMailSender,
                               Clock clock,
                               SecureRandom secureRandom) {
        this.javaMailSender = javaMailSender;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    // URLトークン生成
    public String generateVerificationUrlToken() {
        // これは128bitのbit配列
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // pending_usersに登録するハッシュトークンの生成
    public String hashToken(String urlToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(urlToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    // 認証メール送信
    public void sendVerificationMail(String toEmail, String urlToken) throws MessagingException {
        String verifyUrl = "https://example.com/verify?token=" + urlToken;

        MimeMessage message = javaMailSender.createMimeMessage();
        // メール組み立てに用いる
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Verify your account");

        helper.setText(
                "Click the following link to verify your account:\n" + verifyUrl,
                false
        );

        javaMailSender.send(message);
    }

}
