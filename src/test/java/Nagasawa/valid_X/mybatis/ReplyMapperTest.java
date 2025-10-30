package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.ReplyMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import Nagasawa.valid_X.infra.mybatis.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class ReplyMapperTest {

    @Autowired
    ReplyMapper replyMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    PostMapper postMapper;

    User parentUser;
    Tweet parentTweet;
    Tweet reply1;
    Tweet reply2;
    Instant fixedTime;

    @BeforeEach
    void setup() {
        fixedTime = Instant.parse("2025-10-31T00:00:00Z");

        parentUser = User.builder()
                .username("parent_user")
                .displayName("ParentDisplay")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(parentUser);

        parentTweet = Tweet.builder()
                .userId(parentUser.getId())
                .content("parent tweet")
                .createdAt(fixedTime)
                .build();
        postMapper.insertTweet(parentTweet);

        // 返信ユーザ & ツイート作成
        User replyUser = User.builder()
                .username("reply_user")
                .displayName("ReplyDisplay")
                .createdAt(fixedTime)
                .build();
        userMapper.insertUser(replyUser);

        reply1 = Tweet.builder()
                .userId(replyUser.getId())
                .content("first reply")
                .inReplyToTweetId(parentTweet.getTweetId())
                .createdAt(fixedTime.plusSeconds(60))
                .build();
        postMapper.insertTweet(reply1);

        reply2 = Tweet.builder()
                .userId(replyUser.getId())
                .content("second reply")
                .inReplyToTweetId(parentTweet.getTweetId())
                .createdAt(fixedTime.plusSeconds(120))
                .build();
        postMapper.insertTweet(reply2);
    }

    // parentExists
    @Test
    @DisplayName("正常系: 親ツイートが存在する場合はtrueを返す")
    void parentExists_true() {
        boolean exists = replyMapper.parentExists(parentTweet.getTweetId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("正常系: 存在しないtweetIdはfalseを返す")
    void parentExists_false() {
        boolean exists = replyMapper.parentExists(9999L);
        assertThat(exists).isFalse();
    }

    // findTweet
    @Test
    @DisplayName("正常系: 指定したtweetIdのツイートを正しく取得できる")
    void findTweet_success() {
        Tweet found = replyMapper.findTweet(parentTweet.getTweetId());
        assertThat(found).isNotNull();
        assertThat(found.getTweetId()).isEqualTo(parentTweet.getTweetId());
        assertThat(found.getContent()).isEqualTo("parent tweet");
        assertThat(found.getUserId()).isEqualTo(parentUser.getId());
    }

    // listReplies
    @Test
    @DisplayName("正常系: 親ツイートへの返信一覧を取得できる（昇順でソートされる）")
    void listReplies_success() {
        List<Tweet> replies = replyMapper.listReplies(parentTweet.getTweetId(), 10, 0);
        assertThat(replies).hasSize(2);
        assertThat(replies.get(0).getContent()).isEqualTo("first reply");
        assertThat(replies.get(1).getContent()).isEqualTo("second reply");
    }

    @Test
    @DisplayName("正常系: limitとoffsetを指定してページング取得できる")
    void listReplies_paging() {
        List<Tweet> replies = replyMapper.listReplies(parentTweet.getTweetId(), 1, 1);
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getContent()).isEqualTo("second reply");
    }

    // countReplies
    @Test
    @DisplayName("正常系: 親ツイートの返信数が正しくカウントされる")
    void countReplies_success() {
        long count = replyMapper.countReplies(parentTweet.getTweetId());
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("正常系: 存在しない親ツイートに対しては0を返す")
    void countReplies_notFound() {
        long count = replyMapper.countReplies(9999L);
        assertThat(count).isEqualTo(0L);
    }
}