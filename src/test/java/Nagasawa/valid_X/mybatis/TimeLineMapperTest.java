package Nagasawa.valid_X.mybatis;

import Nagasawa.valid_X.domain.dto.GetMediaResult;
import Nagasawa.valid_X.domain.dto.TweetView;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import Nagasawa.valid_X.domain.model.User;
import Nagasawa.valid_X.infra.mybatis.mapper.*;
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
public class TimeLineMapperTest {

    @Autowired
    private TimelineMapper timelineMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private TweetMetricsMapper tweetMetricsMapper;
    @Autowired
    private FollowMapper followMapper;

    User viewer;
    User followee;
    Tweet tweet1;
    Tweet tweet2;

    @BeforeEach
    void setup() {
        Instant fixedTime = Instant.parse("2025-10-13T12:00:00Z");
        // viewer がポストを取得する設定
        viewer = User.builder()
                .username("user_viewer")
                .displayName("ViewerDisplay")
                .createdAt(fixedTime)
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .build();
        userMapper.insertUser(viewer);

        // followeeはviewerをフォローしている
        followee = User.builder()
                .username("user_followee")
                .displayName("FolloweeDisplay")
                .createdAt(fixedTime)
                .locale("ja-JP")
                .timezone("Asia/Tokyo")
                .build();
        userMapper.insertUser(followee);

        // followeeがviewerをフォローしている
        followMapper.insert(viewer.getId(), followee.getId());

        // viewerのポスト
        tweet1 = Tweet.builder()
                .userId(viewer.getId())
                .content("hello viewer")
                .build();
        postMapper.insertTweet(tweet1);

        // followeeのポスト
        tweet2 = Tweet.builder()
                .userId(followee.getId())
                .content("hello followee")
                .build();
        postMapper.insertTweet(tweet2);

        // メトリクスの初期化
        tweetMetricsMapper.insertInit(tweet1.getTweetId());
        tweetMetricsMapper.insertInit(tweet2.getTweetId());
    }

    // listHomeTimeLineのテストコード
    @Test
    @DisplayName("正常系: listHomeTimeLineで正しくポストが取得できる")
    void listHomeTimeLine_success() {
        List<TweetView> result = timelineMapper.listHomeTimeline(viewer.getId(), null, 10);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(TweetView::getUserId)
                .contains(viewer.getId(), followee.getId());
        assertThat(result.get(0).getTweetId()).isNotNull();
        assertThat(result.get(0).getContent()).isNotBlank();
    }

    // listUserTweetsのテストコード
    @Test
    @DisplayName("正常系: listUserTweetsで正しくポストが取得できる")
    void listUserTweets_success() {
        List<TweetView> result = timelineMapper.listUserTweets(followee.getId(), null, 10, viewer.getId());

        assertThat(result.size()).isEqualTo(1);
        TweetView found = result.get(0);
        assertThat(found.getContent()).isEqualTo("hello followee");
        assertThat(found.getUserId()).isEqualTo(followee.getId());
        assertThat(found.getTweetId()).isEqualTo(tweet2.getTweetId());
    }

    // listRepliesのテストコード
    @Test
    @DisplayName("正常系: listRepliesで正しくリプライが取得できる")
    void listReplies_success() {
        Tweet reply = Tweet.builder()
                .userId(followee.getId())
                .content("Hello reply")
                .inReplyToTweetId(tweet1.getTweetId())
                .build();
        postMapper.insertTweet(reply);

        List<TweetView> replies = timelineMapper.listReplies(tweet1.getTweetId(), null, 10, viewer.getId());

        assertThat(replies.size()).isEqualTo(1);
        TweetView found = replies.get(0);
        assertThat(found.getUserId()).isEqualTo(followee.getId());
        assertThat(found.getContent()).isEqualTo("Hello reply");
        assertThat(found.getInReplyToTweetId()).isEqualTo(tweet1.getTweetId());
    }

    // listPopularTweetsのテストコード
    @Test
    @DisplayName("正常系: listPopularTweetsでポストがいいね順で取得できる")
    void listPopularTweets_success() {
        tweetMetricsMapper.incrementLike(tweet1.getTweetId());
        tweetMetricsMapper.incrementLike(tweet1.getTweetId());
        tweetMetricsMapper.incrementLike(tweet2.getTweetId());

        // 30年くらいはテストコードとして機能する
        List<TweetView> result = timelineMapper.listPopularTweets(viewer.getId(), null, null, 10, 100000);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getTweetId()).isEqualTo(tweet1.getTweetId());
        assertThat(result.get(0).getLikeCount()).isEqualTo(2);
        assertThat(result.get(1).getTweetId()).isEqualTo(tweet2.getTweetId());
        assertThat(result.get(1).getLikeCount()).isEqualTo(1);

    }

    // listMediaForTweetIdsのテストコード
    @Test
    @DisplayName("正常系: listMediaForTweetIdsで正しくgetMediaResultが取得できる")
    void listMediaForTweetIds_success() {
        Media media = Media.builder()
                .mediaType("image")
                .mimeType("image/png")
                .bytes(2048L)
                .width(800)
                .height(600)
                .storageKey("test/storage/image_1.png")
                .build();
        postMapper.insertMedia(media);

        TweetMedia tweetMedia = TweetMedia.builder()
                .tweetId(tweet1.getTweetId())
                .mediaId(media.getMediaId())
                .build();
        postMapper.insertTweetMedia(tweetMedia);

        List<GetMediaResult> result = timelineMapper.listMediaForTweetIds(List.of(tweet1.getTweetId()));

        assertThat(result.size()).isEqualTo(1);
        GetMediaResult found = result.get(0);
        assertThat(found.getMediaId()).isEqualTo(media.getMediaId());
        assertThat(found.getTweetId()).isEqualTo(tweet1.getTweetId());
        assertThat(found.getStorageKey()).isEqualTo(media.getStorageKey());


    }



}
