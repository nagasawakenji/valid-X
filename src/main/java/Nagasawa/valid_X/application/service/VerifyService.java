package Nagasawa.valid_X.application.service;

import Nagasawa.valid_X.application.mapper.PendingUserConverter;
import Nagasawa.valid_X.domain.dto.VerifyResult;
import Nagasawa.valid_X.domain.model.*;
import Nagasawa.valid_X.event.GenerateAuthTokenRequestEvent;
import Nagasawa.valid_X.exception.goneProblems.TokenExpiredException;
import Nagasawa.valid_X.exception.notFoundProblems.TokenNotFoundProblemException;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyService {
    private final PendingUserMapper pendingUserMapper;
    private final UserMapper userMapper;
    private final Clock clock;
    private final VerificationService verificationService;
    private final PendingUserConverter pendingUserConverter;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Value("${app.jwt.access-ttl}")
    private Duration accessTtl;

    @Transactional
    public VerifyResult verify(String urlToken) {

        // urlTokenをハッシュ化（DBのtoken_hashと比較用）
        String tokenHash = verificationService.hashToken(urlToken);

        // token_hashで pending_users を検索（MyBatis は未ヒット時に null を返すことが多いので Optional でラップ）
        PendingUser pendingUser = java.util.Optional
                .ofNullable(pendingUserMapper.findPendingUserByTokenHash(tokenHash))
                .orElseThrow(() -> new TokenNotFoundProblemException("token not found"));

        Instant now = Instant.now(clock);
        // トークンに関するチェック
        if (pendingUser.getExpiresAt().isBefore(now)) {
            throw new TokenExpiredException("token expired");
        }
        if (pendingUser.isVerified()) {
            // すでに認証済み
            return VerifyResult.alreadyVerified();
        }

        // usersに同様のusername,emailを持つレコードが存在しないことを確認
        if (userMapper.existsByUsername(pendingUser.getUsername()) || userMapper.existsByEmail(pendingUser.getEmail())) {
            // すでに本登録ユーザーに存在する
            return VerifyResult.alreadyVerified();
        }

        // usersへの本登録の実行
        // users → user_emails → profiles → counts → user_passwordsの順番で登録していく
        User user = pendingUserConverter.toUser(pendingUser);
        try {
            // usersへのINSERT
            user.setCreatedAt(now);
            userMapper.insertUser(user);

            // user_emailsへのINSERT
            UserEmail userEmail = pendingUserConverter.toUserEmail(pendingUser);
            userEmail.setUserId(user.getId());
            userMapper.insertUserEmail(userEmail);

            // profilesへのINSERT
            Profile profile = pendingUserConverter.toProfile(pendingUser);
            profile.setUserId(user.getId());
            userMapper.insertProfile(profile);

            // countsへのINSERT
            Count count = pendingUserConverter.toCount(pendingUser);
            count.setUserId(user.getId());
            userMapper.insertCount(count);

            // users_passwordsへのINSERT
            UserPassword userPassword = pendingUserConverter.toUserPasswords(pendingUser);
            userPassword.setUserId(user.getId());
            userMapper.insertUserPassword(userPassword);

            // pending_usersから削除する
            pendingUserMapper.deletePendingUserById(pendingUser.getId());
        } catch (DuplicateKeyException e) {
            // 一意制約違反が発生。ロールバックのために再スローする
            log.warn("Duplicate during verify insert for username={} / email={}. Rolling back transaction.",
                    pendingUser.getUsername(), pendingUser.getEmail(), e);

            // RuntimeException系なので @Transactional によりロールバックされる
            throw e;
        }

        Long userId = user.getId();
        if (userId == null) {
            var created = userMapper.findByUsername(pendingUser.getUsername());
            if (created == null || created.getId() == null) {
                log.error("User inserted but could not be re-fetched by username={}", pendingUser.getUsername());
                throw new TokenNotFoundProblemException("verification failed");
            }
            userId = created.getId();
        }

        // INSERT完了のイベント通知
        applicationEventPublisher.publishEvent(new GenerateAuthTokenRequestEvent(userId, now));

        return VerifyResult.loginMailEnqueued();
    }

}
