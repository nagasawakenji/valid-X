package Nagasawa.valid_X.domain.dto;

import lombok.Value;

/**
 * Like / Unlike 実行後の結果を表す DTO
 */
@Value
public class LikeStatusResult {
    Long tweetId;     // 対象ツイート
    boolean liked;    // 現在のユーザーの like 状態
    long likeCount;   // 最新の like 合計数

    public static LikeStatusResult of(Long tweetId, boolean liked, long likeCount) {
        return new LikeStatusResult(tweetId, liked, likeCount);
    }
}