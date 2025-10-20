package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.RefreshToken;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
import Nagasawa.valid_X.infra.mybatis.mapper.RefreshTokenMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class RefreshTokenMapperTest {

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserSessionMapper userSessionMapper;

    User user;
    RefreshToken token;
    UUID sessionId;
    Long userId;

    @BeforeEach
    void setup() {
        // refresh_tokensにはforeign_key users(id)があるので、usersにあらかじめレコードを保存しておく
        Instant now = Instant.parse("2025-10-13T12:00:00Z");
        user = User.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .createdAt(now)
                .build();
        userMapper.insertUser(user);

        // 同様の理由でuser_sessionも作成する
        UserSession userSession = UserSession.builder()
                .userId(user.getId())
                .sessionVersion(0)
                .createdAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .build();

        userSessionMapper.insert(userSession);
        sessionId = userSession.getId();
        userId = user.getId();


        token = RefreshToken.builder()
                .userId(userId)
                .sessionId(sessionId)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(7)))
                .rotatedFrom(null)
                .revokedAt(null)
                .build();

        refreshTokenMapper.insert(token);
    }

    @Test
    @DisplayName("正常系: idでトークンを取得できる")
    void findById_success() {
        RefreshToken found = refreshTokenMapper.findById(token.getId());

        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("正常系: アクティブトークンをidで取得できる")
    void findActiveById_success() {
        RefreshToken found = refreshTokenMapper.findActiveById(token.getId());

        assertThat(found).isNotNull();
        assertThat(found.getRevokedAt()).isNull();
    }

    @Test
    @DisplayName("正常系: revokeによりトークンが失効する")
    void revoke_success() {
        Instant revokeTime = Instant.parse("2025-10-14T00:00:00Z");

        int updated = refreshTokenMapper.revoke(token.getId(), revokeTime);
        assertThat(updated).isEqualTo(1);

        RefreshToken revoked = refreshTokenMapper.findById(token.getId());
        assertThat(revoked.getRevokedAt()).isEqualTo(revokeTime);
    }

    @Test
    @DisplayName("正常系: revokeBySessionで同一セッションの全トークンが失効する")
    void revokeBySession_success() {
        // 同一sessionIdで別トークンを作成
        RefreshToken another = RefreshToken.builder()
                .userId(userId)
                .sessionId(sessionId)
                .issuedAt(token.getIssuedAt().plusSeconds(10))
                .expiresAt(token.getExpiresAt())
                .rotatedFrom(token.getId())
                .revokedAt(null)
                .build();
        refreshTokenMapper.insert(another);

        Instant revokeTime = Instant.parse("2025-10-15T00:00:00Z");
        int updated = refreshTokenMapper.revokeBySession(sessionId, revokeTime);
        assertThat(updated).isEqualTo(2);

        RefreshToken revoked1 = refreshTokenMapper.findById(token.getId());
        RefreshToken revoked2 = refreshTokenMapper.findById(another.getId());

        assertThat(revoked1.getRevokedAt()).isEqualTo(revokeTime);
        assertThat(revoked2.getRevokedAt()).isEqualTo(revokeTime);
    }

    @Test
    @DisplayName("正常系: deleteExpiredで期限切れトークンが削除される")
    void deleteExpired_success() {
        Instant oldTime = Instant.parse("2025-10-01T00:00:00Z");

        // 有効期限切れトークンを作成
        RefreshToken expired = RefreshToken.builder()
                .userId(userId)
                .sessionId(sessionId)
                .issuedAt(oldTime)
                .expiresAt(oldTime.plus(Duration.ofDays(3)))
                .rotatedFrom(null)
                .revokedAt(null)
                .build();
        refreshTokenMapper.insert(expired);

        int deleted = refreshTokenMapper.deleteExpired(Instant.parse("2025-10-10T00:00:00Z"));
        assertThat(deleted).isEqualTo(1);
    }
}