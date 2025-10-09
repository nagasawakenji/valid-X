package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.TimelineQueryService;
import Nagasawa.valid_X.domain.dto.GetPostResult;
import Nagasawa.valid_X.domain.dto.Page;
import Nagasawa.valid_X.domain.dto.TweetView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class TimelineController {

    private final TimelineQueryService timelineQueryService;

    // ホームTL
    @GetMapping("/timeline")
    public ResponseEntity<Page<GetPostResult>> homeTimeline(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        Long viewerId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(timelineQueryService.homeTimeline(viewerId, cursor, limit));
    }

    // ツイートのリプライ一覧
    @GetMapping("/tweets/{tweetId}/replies")
    public ResponseEntity<Page<GetPostResult>> replies(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tweetId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        Long viewerId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(timelineQueryService.replies(tweetId, cursor, limit, viewerId));
    }

    // ユーザー別タイムライン
    @GetMapping("/users/{userId}/tweets")
    public ResponseEntity<Page<GetPostResult>> userTweets(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") Long targetUserId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        Long viewerId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(timelineQueryService.userTweets(targetUserId, cursor, limit, viewerId));
    }

    // 直近N日(default:15日)で人気のツイートを順に取得
    @GetMapping("/tweets/popular")
    public ResponseEntity<Page<GetPostResult>> popularTweets(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "cursor_like", required = false) Long cursorLike,
            @RequestParam(value = "cursor_id",   required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "30") int limit,
            @RequestParam(value = "day_count", defaultValue = "15") int dayCount
    ) {
        Long viewerId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(timelineQueryService.popularTweets(viewerId, cursorLike, cursorId, limit, dayCount));
    }
}