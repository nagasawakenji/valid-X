package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.LikeService;
import Nagasawa.valid_X.domain.dto.LikeStatusResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/tweets", produces = MediaType.APPLICATION_JSON_VALUE)
public class LikeController {

    private final LikeService likeService;

    // いいね（冪等）: PUT /v1/tweets/{tweetId}/like
    @PutMapping("/{tweetId}/like")
    public ResponseEntity<LikeStatusResult> like(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId
    ) {
        if (tweetId == null || tweetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = Long.valueOf(jwt.getSubject());
        LikeStatusResult result = likeService.like(tweetId, userId);
        return ResponseEntity.ok(result);
    }

    // いいね解除（冪等）: DELETE /v1/tweets/{tweetId}/like
    @DeleteMapping("/{tweetId}/like")
    public ResponseEntity<LikeStatusResult> unlike(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId
    ) {
        if (tweetId == null || tweetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = Long.valueOf(jwt.getSubject());
        LikeStatusResult result = likeService.unlike(tweetId, userId);
        return ResponseEntity.ok(result);
    }
}