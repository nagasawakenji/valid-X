package Nagasawa.valid_X.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value @Builder
public class TweetView {
    Long tweetId;
    Long userId;
    String username;         // JOIN users.name（最低限の作者情報）
    String content;
    Long inReplyToTweetId;
    Instant createdAt;

    long likeCount;          // tweet_metrics から
    long repostCount;
    long replyCount;

    Boolean likedByMe;       // 認証ユーザー視点のフラグ（任意拡張）
    Boolean repostedByMe;
}