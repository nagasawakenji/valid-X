package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.MagicLink;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.MagicLinkMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class MagicLinkMapperTest {

    @Autowired
    private MagicLinkMapper magicLinkMapper;
    @Autowired
    private UserMapper userMapper; // ユーザーIDを取得するために使用

    // テスト用の固定時刻設定
    private final Instant fixedTimeFuture = Instant.parse("2100-10-13T12:00:00Z");
    private final Instant fixedTimePast = Instant.parse("2000-10-13T12:00:00Z");
    // テスト用のHMAC Key ID
    private final short KEY_ID = 1;

    // テストで使用するユーザーとトークンハッシュ
    private User testUser;
    private final byte[] VALID_TOKEN_HASH = "valid-token-hash-1".getBytes();
    private final byte[] OTHER_TOKEN_HASH = "other-token-hash-2".getBytes();

    @BeforeEach
    void setup() {
        // テスト用のユーザーを作成し、IDを取得する
        testUser = User.builder()
                .username("test_magic_link_user")
                .displayName("MagicLinkDisplay")
                .createdAt(fixedTimeFuture)
                .build();
        userMapper.insertUser(testUser);
    }

    // insertのテスト
    @Test
    @DisplayName("正常系: MagicLinkが正しくDBに挿入される")
    void insert_success() {
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimeFuture.plus(1, ChronoUnit.HOURS))
                .usedAt(null)
                .build();

        int inserted = magicLinkMapper.insert(link);
        assertThat(inserted).isEqualTo(1);
        // IDがDBによって生成されていることを確認
        assertThat(link.getId()).isNotNull();
    }

    // consumeAndReturnUserId のテスト

    @Test
    @DisplayName("正常系: 有効なトークンを消費し、ユーザーIDを返す")
    void consumeAndReturnUserId_success() {
        // Arrange: 未使用で未来に有効期限が切れるマジックリンクを挿入
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimeFuture.plus(1, ChronoUnit.HOURS))
                .usedAt(null)
                .build();
        magicLinkMapper.insert(link);

        // Act: トークンを消費
        Long userId = magicLinkMapper.consumeAndReturnUserId(VALID_TOKEN_HASH, KEY_ID);

        // Assert: ユーザーIDが正しく返され、消費済み状態に更新されたことを確認 (UPDATE COUNT = 1 のため戻り値は user_id)
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("異常系: すでに使用済みのトークンは消費できない（冪等性）")
    void consumeAndReturnUserId_alreadyUsed() {
        // Arrange: 既に使用済みのマジックリンクを挿入 (DBの時間関数 now() をモックできないため、usedAt に固定時刻を入れる)
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimePast.plus(1, ChronoUnit.HOURS))
                .usedAt(fixedTimePast) // すでに使用済みとして挿入
                .build();
        magicLinkMapper.insert(link);

        // Act: トークンを消費しようとする
        Long userId = magicLinkMapper.consumeAndReturnUserId(VALID_TOKEN_HASH, KEY_ID);

        // Assert: ユーザーIDは返されない
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("異常系: 有効期限切れのトークンは消費できない")
    void consumeAndReturnUserId_expired() {
        // Arrange: 過去に有効期限が切れたマジックリンクを挿入
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimePast.minus(1, ChronoUnit.HOURS)) // 既に期限切れ
                .usedAt(null)
                .build();
        magicLinkMapper.insert(link);

        // Act: トークンを消費しようとする
        Long userId = magicLinkMapper.consumeAndReturnUserId(VALID_TOKEN_HASH, KEY_ID);

        // Assert: ユーザーIDは返されない
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("異常系: トークンハッシュが一致しない場合は消費できない")
    void consumeAndReturnUserId_hashMismatch() {
        // Arrange: 有効なマジックリンクを挿入
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimeFuture.plus(1, ChronoUnit.HOURS))
                .usedAt(null)
                .build();
        magicLinkMapper.insert(link);

        // Act: 異なるハッシュで消費しようとする
        Long userId = magicLinkMapper.consumeAndReturnUserId(OTHER_TOKEN_HASH, KEY_ID);

        // Assert: ユーザーIDは返されない
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("異常系: hmac_key_idが一致しない場合は消費できない")
    void consumeAndReturnUserId_keyIdMismatch() {
        // Arrange: KEY_ID = 1 でマジックリンクを挿入
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimeFuture.plus(1, ChronoUnit.HOURS))
                .usedAt(null)
                .build();
        magicLinkMapper.insert(link);

        // Act: 異なる KEY_ID = 99 で消費しようとする
        Long userId = magicLinkMapper.consumeAndReturnUserId(VALID_TOKEN_HASH, (short) 99);

        // Assert: ユーザーIDは返されない
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("制約系: 参照ユーザーが削除されると、ON DELETE CASECADEによりマジックリンクも削除される")
    void constraint_fKey_userId() {
        // Arrange: マジックリンクを作成
        MagicLink link = MagicLink.builder()
                .userId(testUser.getId())
                .tokenHash(VALID_TOKEN_HASH)
                .hmacKeyId(KEY_ID)
                .expiresAt(fixedTimeFuture.plus(1, ChronoUnit.HOURS))
                .usedAt(null)
                .build();
        magicLinkMapper.insert(link);

        userMapper.deleteUser(testUser);
        Long userId = magicLinkMapper.consumeAndReturnUserId(VALID_TOKEN_HASH, KEY_ID);
        assertThat(userId).isNull();
    }
}
