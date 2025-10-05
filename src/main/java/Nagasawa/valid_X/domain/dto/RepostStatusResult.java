package Nagasawa.valid_X.domain.dto;

import lombok.Value;

@Value
public class RepostStatusResult {

    Long tweetId; // repostしたpostのtweetId
    boolean reposted; // repostの状態
    long repostCount;

    public static RepostStatusResult of(Long tweetId, boolean reposted, long repostCount) {
        return new RepostStatusResult(tweetId, reposted, repostCount);
    }
}
