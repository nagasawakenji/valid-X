package Nagasawa.valid_X.application.service;


import Nagasawa.valid_X.application.service.JwtService;
import Nagasawa.valid_X.domain.dto.AccessIssueResult;
import Nagasawa.valid_X.domain.model.RefreshToken;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserSessionMapper sessionMapper;
    private final RefreshTokenMapper refreshMapper;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final Clock clock;

    @Value("${app.auth.access-ttl-minutes:15}")
    private long accessTtlMinutes;

    @Value("${app.auth.refresh-ttl-days:30}")
    private long refreshTtlDays;

    // Cookie 属性
    @Value("${app.cookies.refresh.name:refresh_token}")
    private String refreshCookieName;
    @Value("${app.cookies.refresh.path:/v1/auth}")
    private String refreshCookiePath;
    @Value("${app.cookies.refresh.domain:}") // 例: .example.com（空なら未設定）
    private String refreshCookieDomain;

    @Transactional
    public AccessIssueResult issueForUser(Long userId) {
        Instant now = Instant.now(clock);

        User user = userMapper.findById(userId);

        // 1) セッション作成
        UserSession session = UserSession.builder()
                .userId(userId)
                .sessionVersion(1)
                .createdAt(now)
                .expiresAt(null)
                .build();
        sessionMapper.insert(session);

        // 2) Refresh 発行
        RefreshToken rt = RefreshToken.builder()
                .userId(userId)
                .sessionId(session.getId())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(refreshTtlDays)))
                .build();
        refreshMapper.insert(rt);

        // 3) Access JWT
        String access = jwtService.issueAccessToken(
                userId,
                user.getUsername(),
                session.getId(),
                session.getSessionVersion(),
                null
        );
        long accessTtlSec = Duration.ofMinutes(accessTtlMinutes).toSeconds();

        // 4) Refresh Cookie を組み立てる（レスポンスにはまだ積まない）
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, rt.getId().toString())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(refreshCookiePath)
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        return new AccessIssueResult(access, accessTtlSec, cookie);
    }
}