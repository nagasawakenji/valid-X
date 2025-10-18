package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.FollowService;
import Nagasawa.valid_X.domain.dto.FollowStatusResult;
import Nagasawa.valid_X.domain.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PutMapping("/{userId}/follow")
    public ResponseEntity<FollowStatusResult> follow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") Long userId
    ) {
        Long viewerId = Long.valueOf(jwt.getSubject());
        if (userId == null || userId <= 0) return ResponseEntity.badRequest().build();
        FollowStatusResult result = followService.follow(viewerId, userId);
        // 204 No Content でも良いが、ここでは状態を返す
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<FollowStatusResult> unfollow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") Long userId
    ) {
        Long viewerId = Long.valueOf(jwt.getSubject());
        if (userId == null || userId <= 0) return ResponseEntity.badRequest().build();
        FollowStatusResult result = followService.unfollow(viewerId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummary>> followers(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        List<UserSummary> userSummaries = followService.listFollowers(userId, cursor, limit);

        return ResponseEntity.ok(userSummaries);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummary>> following(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        List<UserSummary> userSummaries = followService.listFollowing(userId, cursor, limit);

        return ResponseEntity.ok(userSummaries);
    }

}