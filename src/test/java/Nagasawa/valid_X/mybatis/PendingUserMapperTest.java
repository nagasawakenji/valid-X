package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.PendingUser;
import Nagasawa.valid_X.infra.mybatis.mapper.PendingUserMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class PendingUserMapperTest {

    @Autowired
    private PendingUserMapper pendingUserMapper;

    PendingUser pendingUser;
    byte[] tokenHash;

    @BeforeEach
    void setup() {
        Instant testTime = Instant.parse("2025-10-13T12:00:00Z");
        Instant now = Instant.parse("2025-10-13T12:00:01Z");
        tokenHash = new byte[]{1, 2, 3};
        pendingUser = PendingUser.builder()
                .username("test_user")
                .displayName("TestDisplay")
                .email("test@example.com")
                .passwordHash("pass-hashed")
                .tokenHash(tokenHash)
                .lastSentAt(testTime)
                .expiresAt(testTime.plus(Duration.ofMinutes(15)))
                .lastSentAt(testTime)
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .build();

        pendingUserMapper.insertPendingUser(pendingUser);
    }

    @Test
    @DisplayName("正常系: idで正しく検索することができる")
    void findPendingUserById_success() {
        PendingUser found = pendingUserMapper.findPendingUserById(pendingUser.getId());

        assertThat(found.getId()).isEqualTo(pendingUser.getId());
        assertThat(found.getTokenHash()).isEqualTo(tokenHash);
    }

    @Test
    @DisplayName("正常系: tokenHashで正しく検索することができる")
    void findPendingUserBytokenHash_success() {
        PendingUser found = pendingUserMapper.findPendingUserByTokenHash(tokenHash);

        assertThat(found.getId()).isEqualTo(pendingUser.getId());
        assertThat(found.getTokenHash()).isEqualTo(tokenHash);
    }

    @Test
    @DisplayName("正常系: emailで存在判定ができる")
    void existsActiveByEmail_success() {
        assertThat(pendingUserMapper.existsActiveByEmail("test@example.com")).isTrue();
    }

    @Test
    @DisplayName("正常系: 一意制約によりupdate判定となる")
    void insertPendingUser_fail_duplicate() {
        int inserted = pendingUserMapper.insertPendingUser(pendingUser);

        assertThat(inserted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: updateが正しく実行できる")
    void updatePendingUser_success() {
        // 現状使用していないが、verifiedしか更新することがない設計なので
        // verifiedのみをupdateする
        PendingUser updatedPendingUser = PendingUser.builder()
                .id(pendingUser.getId())
                .username("test_user")
                .displayName("TestDisplay")
                .email("test@example.com")
                .passwordHash("pass-hashed")
                .tokenHash(tokenHash)
                .lastSentAt(pendingUser.getLastSentAt())
                .expiresAt(pendingUser.getExpiresAt())
                .lastSentAt(pendingUser.getLastSentAt())
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .verified(true)
                .build();

        int updated = pendingUserMapper.updatePendingUser(updatedPendingUser);
        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: idで正しく削除が実行できる")
    void deletePendingUser_success() {
        int deleted = pendingUserMapper.deletePendingUserById(pendingUser.getId());

        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("正常系: verified指定で正しく削除が実行できる")
    void deletePendingUsers_success() {
        PendingUser expectedDelete = PendingUser.builder()
                .username("test_user_2")
                .displayName("TestDisplay2")
                .email("test2@example.com")
                .passwordHash("pass-hashed")
                .tokenHash(tokenHash)
                .lastSentAt(pendingUser.getLastSentAt())
                .expiresAt(pendingUser.getExpiresAt())
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .verified(true)
                .build();

        pendingUserMapper.insertPendingUser(expectedDelete);

        int deleted = pendingUserMapper.deletePendingUsers(true);
        assertThat(deleted).isEqualTo(1);
    }


}
