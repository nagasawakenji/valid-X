package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.dto.RefreshResult;
import Nagasawa.valid_X.domain.model.RefreshToken;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.exception.invalidProblems.InvalidRefreshTokenProblemException;
import Nagasawa.valid_X.exception.invalidProblems.InvalidSessionProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.MagicLinkMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshService {
    private final MagicLinkMapper magicLinkMapper;
    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserSessionMapper userSessionMapper;
    private final Clock clock;
    private final JwtService jwtService;

    @Value("${app.auth.access-ttl-minutes:15}")
    private long accessTtlMinutes;

    @Value("${app.auth.refresh-ttl-days:30}")
    private long refreshTtlDays;

    // Cookie 属性
    @Value("${app.cookies.refresh.name:refresh_token}")
    private String refreshCookieName;
    @Value("${app.cookies.refresh.path:/v1/auth}")
    private String refreshCookiePath;

    @Transactional
    public RefreshResult refresh(UUID refreshId) {

        Instant now = Instant.now(clock);

        // RefreshToken取得
        RefreshToken rt = refreshTokenMapper.findActiveById(refreshId);
        if (rt == null || rt.getRevokedAt() != null || !rt.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenProblemException("invalid_refresh_token");
        }


        // UserSession取得
        UserSession session = userSessionMapper.findById(rt.getSessionId());
        if (session == null || session.getRevokedAt() != null || session.getExpiresAt() == null
                || !session.getExpiresAt().isAfter(now)) {
            throw new InvalidSessionProblemException("invalid_session_token");
        }

        // RefreshTokenをrevoke
        refreshTokenMapper.revoke(rt.getId(), now);

        RefreshToken newRt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(rt.getUserId())
                .issuedAt(now)
                .sessionId(rt.getSessionId())
                .expiresAt(now.plus(Duration.ofDays(refreshTtlDays)))
                .rotatedFrom(rt.getId())
                .build();
        refreshTokenMapper.insert(newRt);

        User user = userMapper.findById(rt.getUserId());

        String jwt = jwtService.issueAccessToken(
                rt.getUserId(),
                user.getUsername(),
                session.getId(),
                session.getSessionVersion(),
                null
        );

        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, newRt.getId().toString())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(refreshCookiePath)
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        return new RefreshResult(jwt, Duration.ofMinutes(accessTtlMinutes).getSeconds(), cookie);
    }
}
