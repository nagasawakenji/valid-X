package Nagasawa.valid_X.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class VerificationService {
    // 認証に用いるハッシュトークンなどに関するロジックを取りまとめる

    private final JavaMailSender javaMailSender;
    private final SecureRandom secureRandom;

    // URLトークン生成
    public String generateVerificationUrlToken() {
        // これは128bitのbit配列
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // ローカルでcurlを叩くため、一時的にlogに出す
        // 後で必ず消すこと!!!!!!!!
        log.info("[DEV] verification token = {}", token);
        return token;
    }

    // pending_usersに登録するハッシュトークンの生成
    public byte[] hashToken(String urlToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(urlToken.getBytes(StandardCharsets.UTF_8));
            return hashed;
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    // 認証メール送信
    public void sendVerificationMail(String toEmail, String urlToken) throws MessagingException {
        String verifyUrl = "https://localhost:8443/v1/auth/verify?token=" + urlToken;

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
