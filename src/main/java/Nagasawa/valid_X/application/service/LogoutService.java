package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.domain.dto.LogoutResult;
import Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final UserMapper userMapper;
    private final UserSessionMapper userSessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final Clock clock;

    @Transactional
    public LogoutResult logout(Long userId, UUID sessionUuid) {

        Instant now = Instant.now(clock);

        User user = userMapper.findById(userId);
        if (user == null) {
            // userが存在しない
            throw new NotFoundProblemException("user_not_found");
        }

        UserSession session = userSessionMapper.findById(sessionUuid);
        if (session == null || !session.getUserId().equals(userId)) {
            // セッションが存在しない or 他人のセッション
            throw new NotFoundProblemException("session_not_found");
        }

        // sessionのrevokedフラグ
        boolean alreadyRevoked = (session.getRevokedAt() != null);

        Instant revokedAt;
        if (!alreadyRevoked) {
            int sessionRevoked = userSessionMapper.revoke(sessionUuid, now);
            if (sessionRevoked == 1) {
                // 正しくrevokeされた
                revokedAt = now;
            } else {
                // 別の機能によりすでにrevokeされた場合
                revokedAt = session.getRevokedAt();
            }
        } else {
            revokedAt = session.getRevokedAt();
        }

        // refresh_tokenのrevoke
        int refreshRevoked = refreshTokenMapper.revokeBySession(sessionUuid, now);

        log.info("logout: userId={}, sessionId={}, alreadyRevoked={}, revokedAt={}, refreshRevoked={}",
                userId, sessionUuid, alreadyRevoked, revokedAt, refreshRevoked);

        return LogoutResult.of(
                userId,
                sessionUuid,
                alreadyRevoked,
                revokedAt,
                refreshRevoked
        );


    }
}
