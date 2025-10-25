package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.HmacKeyProvider;
import Nagasawa.valid_X.domain.dto.MagicLinkIssue;
import Nagasawa.valid_X.infra.mybatis.mapper.MagicLinkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneTimeLoginTokenService {

    private final MagicLinkMapper magicLinkMapper;
    private final HmacKeyProvider hmacKeyProvider;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public record MagicLink(String url, Instant expiresAt) {}

    // ログイン用マジックリンクの発行+DBへの保存
    public MagicLinkIssue issueLoginLink(Long userId, Duration ttl, String appBaseUrl) {
        short kid = hmacKeyProvider.activeKeyId();

        // 生トークンの生成
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        byte[] raw = ByteBuffer.allocate(2 + bytes.length).putShort(kid).put(bytes).array();

        // 使用期限
        Instant exp = Instant.now(clock).plus(ttl);

        // 保存用ハッシュトークンの生成
        byte[] tokenHash = hmac(raw, hmacKeyProvider.keyOf(kid));

        Nagasawa.valid_X.domain.model.MagicLink magicLink = Nagasawa.valid_X.domain.model.MagicLink.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .hmacKeyId(kid)
                .expiresAt(exp)
                .build();

        magicLinkMapper.insert(magicLink);

        // URL生成
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        // 検証用
        // あとは必ず消すこと!!!!!!
        log.info("[DEV] magic token (base64url) = {}", token);

        String url = appBaseUrl.endsWith("/")
                ? appBaseUrl + "v1/auth/magic-link/consume?token=" + token
                : appBaseUrl + "/v1/auth/magic-link/consume?token=" + token;

        return MagicLinkIssue.builder()
                .url(url)
                .expiresAt(exp)
                .build();
    }

    // マジックリンク消費メソッド
    public Long consume(String base64Token) {
        byte[] raw;
        try { raw = Base64.getUrlDecoder().decode(base64Token); }
        catch (IllegalArgumentException e) { return null; }

        if (raw.length < 2) return null;
        short kid = ByteBuffer.wrap(raw, 0, 2).getShort();
        SecretKey key = hmacKeyProvider.keyOf(kid);

        if (key == null) return null;

        byte[] hashToken = hmac(raw, key);
        return magicLinkMapper.consumeAndReturnUserId(hashToken, kid);
    }

    // ハッシュ化メソッド
    private static byte[] hmac(byte[] data, SecretKey key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failure", e);
        }
    }
}
