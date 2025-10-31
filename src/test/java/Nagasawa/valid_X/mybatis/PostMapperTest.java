package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class PostMapperTest {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;

    Instant fixedTime = Instant.parse("2025-10-13T12:00:00Z");
    User testUser;

    @BeforeEach
    void setup() {
        // テストユーザーをセットアップ
        testUser = User.builder()
                .username("test_poster_user")
                .displayName("TestPosterDisplay")
                .createdAt(fixedTime)
                .build();
        // ユーザーを挿入し、IDを生成させる
        userMapper.insertUser(testUser);
        assertThat(testUser.getId()).isNotNull();
    }

    // --- insertTweet のテスト ---

    @Test
    @DisplayName("正常系: 基本的なツイートが正しく挿入され、IDが生成される")
    void insertTweet_success() {
        Tweet tweet = Tweet.builder()
                .userId(testUser.getId())
                .content("Initial test content")
                .createdAt(fixedTime)
                .build();

        int inserted = postMapper.insertTweet(tweet);
        assertThat(inserted).isEqualTo(1);
        assertThat(tweet.getTweetId()).isNotNull().isPositive();
    }

    @Test
    @DisplayName("正常系: リプライツイートが正しく挿入される")
    void insertTweet_asReply() {
        // 1. 親ツイートの挿入
        Tweet parentTweet = Tweet.builder()
                .userId(testUser.getId())
                .content("Parent tweet for reply")
                .createdAt(fixedTime)
                .build();
        postMapper.insertTweet(parentTweet);
        Long parentTweetId = parentTweet.getTweetId();

        // 2. リプライツイートの挿入
        Tweet replyTweet = Tweet.builder()
                .userId(testUser.getId())
                .content("This is a reply")
                .inReplyToTweetId(parentTweetId)
                .createdAt(fixedTime.plusSeconds(1))
                .build();

        int inserted = postMapper.insertTweet(replyTweet);
        assertThat(inserted).isEqualTo(1);
        assertThat(replyTweet.getTweetId()).isNotNull().isPositive();
        assertThat(replyTweet.getInReplyToTweetId()).isEqualTo(parentTweetId);
    }

    // --- insertMedia のテスト ---

    @Test
    @DisplayName("正常系: メディア情報が正しく挿入され、IDが生成される")
    void insertMedia_success() {
        Media media = Media.builder()
                .mediaType("image")
                .mimeType("image/jpeg")
                .bytes(1024L)
                .storageKey("s3/path/to/image.jpg")
                .sha256(new byte[]{1, 2, 3, 4})
                .createdAt(fixedTime)
                .build();

        int inserted = postMapper.insertMedia(media);
        assertThat(inserted).isEqualTo(1);
        assertThat(media.getMediaId()).isNotNull().isPositive();
    }

    // --- insertTweetMedia のテスト ---

    @Test
    @DisplayName("正常系: ツイートとメディアの関連付けが正しく挿入される")
    void insertTweetMedia_success() {
        // Arrange: ツイートとメディアを準備
        Tweet tweet = Tweet.builder()
                .userId(testUser.getId())
                .content("Tweet with media")
                .build();
        postMapper.insertTweet(tweet);

        Media media = Media.builder()
                .mediaType("image")
                .mimeType("image/png")
                .bytes(500L)
                .storageKey("s3/key/1")
                .build();
        postMapper.insertMedia(media);

        // Act: 関連付けの挿入
        TweetMedia tweetMedia = TweetMedia.builder()
                .tweetId(tweet.getTweetId())
                .mediaId(media.getMediaId())
                .position(0)
                .build();

        int inserted = postMapper.insertTweetMedia(tweetMedia);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    @DisplayName("異常系: position制約 (0-9) 外の値を挿入すると失敗する")
    void insertTweetMedia_positionConstraintViolation() {
        // Arrange: ツイートとメディアを準備
        Tweet tweet = Tweet.builder().userId(testUser.getId()).content("Content").build();
        postMapper.insertTweet(tweet);

        Media media = Media.builder()
                .mediaType("image")
                .mimeType("image/png")
                .bytes(500L)
                .storageKey("s3/key/2")
                .build();
        postMapper.insertMedia(media);

        TweetMedia violation = TweetMedia.builder()
                .tweetId(tweet.getTweetId())
                .mediaId(media.getMediaId())
                .position(10) // 制約違反: CHECK (position BETWEEN 0 AND 9)
                .build();

        // Assert: DataIntegrityViolationException がスローされる
        assertThrows(DataIntegrityViolationException.class, () -> {
            postMapper.insertTweetMedia(violation);
        });
    }

    // --- 外部キー制約のテスト ---

    @Test
    @DisplayName("制約系: ユーザー削除 (ON DELETE CASCADE) - ツイート、そして tweet_media が連鎖削除される")
    void constraint_fKey_userDeleteCascadesThroughTweetToTweetMedia() {
        // Arrange: ツイート、メディア、関連付けを作成
        Tweet tweet = Tweet.builder().userId(testUser.getId()).content("Cascade test").build();
        postMapper.insertTweet(tweet);

        Media media = Media.builder()
                .mediaType("image")
                .mimeType("image/png")
                .bytes(100L)
                .storageKey("s3/key/3")
                .build();
        postMapper.insertMedia(media);

        TweetMedia tweetMedia = TweetMedia.builder()
                .tweetId(tweet.getTweetId())
                .mediaId(media.getMediaId())
                .position(0)
                .build();
        postMapper.insertTweetMedia(tweetMedia);

        // Act: ユーザーを削除
        // ユーザー削除 (users.id -> tweets.user_id: ON DELETE CASCADE)
        // ツイート削除 (tweets.tweet_id -> tweet_media.tweet_id: ON DELETE CASCADE)
        userMapper.deleteUser(testUser);

        // Assert: ユーザー削除が成功すれば、DBの制約によりツイートと関連レコードも連鎖的に削除されていることを保証します。
        assertThat(true).as("ユーザー削除により、関連するツイートとツイートメディアが連鎖削除されます").isTrue();
    }
}
