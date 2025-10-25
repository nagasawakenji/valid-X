package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.service.mail.SmtpMailService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MagicLinkService {
    private final OneTimeLoginTokenService tokenService;
    private final SmtpMailService mailService;
    @Value("${app.frontend.base-url}")
    private String appBaseUrl;

    public void issueAndSend(Long userId, String email) {
        // トークン生成
        var issue = tokenService.issueLoginLink(userId, Duration.ofMinutes(30), appBaseUrl);

        // メール送信
        mailService.sendMagicLoginLink(email, issue.getUrl(), issue.getExpiresAt());

        log.info("Issued magic link for userId={} expiresAt={}", userId, issue.getExpiresAt());
    }
}