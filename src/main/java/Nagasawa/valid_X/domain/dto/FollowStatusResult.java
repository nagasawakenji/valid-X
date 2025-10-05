package Nagasawa.valid_X.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FollowStatusResult {
    Long targetUserId;
    boolean following;
    long followerCount;
    long followingCount;
}