package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.domain.model.UserSession;
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
public class UserSessionMapperTest {

    @Autowired
    private UserSessionMapper userSessionMapper;
    @Autowired
    private UserMapper userMapper;

    UserSession userSession;
    Instant fixedTime;

    @BeforeEach
    void setup() {
        fixedTime = Instant.parse("2025-10-13T12:00:00Z");
        User user = User.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(user);

        userSession = UserSession.builder()
                .userId(user.getId())
                .sessionVersion(1)
                .createdAt(fixedTime)
                .expiresAt(fixedTime.plus(Duration.ofDays(30)))
                .build();
    }

    // insertのテストコード
    @Test
    @DisplayName("正常系: 正しくINSERTされて、uuidが返る")
    void insert_success() {
        int inserted = userSessionMapper.insert(userSession);

        assertThat(inserted).isEqualTo(1);
        assertThat(userSession.getId()).isInstanceOf(UUID.class);
    }

    // findByIdのテストコード
    @Test
    @DisplayName("正常系: uuidでsessionの検索ができる")
    void findById_success() {
        int inserted = userSessionMapper.insert(userSession);
        assertThat(inserted).isEqualTo(1);

        UUID uuid = userSession.getId();
        UserSession found = userSessionMapper.findById(uuid);
        assertThat(found)
                .usingRecursiveComparison()
                .isEqualTo(userSession);
    }

    @Test
    @DisplayName("異常系: 存在しないUUIDで検索した場合はnullを返す")
    void findById_notFound() {
        UserSession found = userSessionMapper.findById(UUID.randomUUID());
        assertThat(found).isNull();
    }

    // updateLastSeenのテストコード
    @Test
    @DisplayName("正常系: 正しくlastSeenAtが更新できる")
    void updateLastSeen_success() {
        Instant newFixedTime = fixedTime.plusSeconds(1);
        int inserted = userSessionMapper.insert(userSession);
        assertThat(inserted).isEqualTo(1);

        int updated = userSessionMapper.updateLastSeen(userSession.getId(), newFixedTime);
        assertThat(updated).isEqualTo(1);

        UserSession found = userSessionMapper.findById(userSession.getId());
        assertThat(found.getLastSeenAt()).isEqualTo(newFixedTime);
    }

    @Test
    @DisplayName("異常系: 存在しないUUIDを指定した場合は更新件数0になる")
    void updateLastSeen_notFound() {
        int updated = userSessionMapper.updateLastSeen(UUID.randomUUID(), fixedTime);
        assertThat(updated).isZero();
    }

    // bumpVersionのテストコード
    @Test
    @DisplayName("正常系: 正しくsessionVersionを+1できる")
    void bumpVersion_success() {
        int inserted = userSessionMapper.insert(userSession);
        assertThat(inserted).isEqualTo(1);

        int bumped = userSessionMapper.bumpVersion(userSession.getId());
        assertThat(bumped).isEqualTo(1);

        UserSession found = userSessionMapper.findById(userSession.getId());
        assertThat(found.getSessionVersion()).isEqualTo(2);
    }

    // revokeのテストコード
    @Test
    @DisplayName("正常系: revokeが正しく実行され、revokedAtが更新される")
    void revoke_success() {
        Instant newFixedTime = fixedTime.plusSeconds(1);
        int inserted = userSessionMapper.insert(userSession);
        assertThat(inserted).isEqualTo(1);

        int revoked = userSessionMapper.revoke(userSession.getId(), newFixedTime);
        assertThat(revoked).isEqualTo(1);

        UserSession found = userSessionMapper.findById(userSession.getId());
        assertThat(found.getRevokedAt()).isEqualTo(newFixedTime);
    }


}
