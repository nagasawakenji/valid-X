package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.RepostService;
import Nagasawa.valid_X.domain.dto.RepostStatusResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/tweets")
@Validated
public class RepostController {

    private final RepostService repostService;

    /** リポスト（冪等）: PUT /v1/tweets/{tweetId}/repost */
    @PutMapping("/{tweetId}/repost")
    public ResponseEntity<RepostStatusResult> repost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId
    ) {
        if (tweetId == null || tweetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = Long.valueOf(jwt.getSubject());
        var result = repostService.repost(tweetId, userId);
        return ResponseEntity.ok(result);
    }

    /** リポスト解除（冪等）: DELETE /v1/tweets/{tweetId}/repost */
    @DeleteMapping("/{tweetId}/repost")
    public ResponseEntity<RepostStatusResult> unrepost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId
    ) {
        if (tweetId == null || tweetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = Long.valueOf(jwt.getSubject());
        var result = repostService.unrepost(tweetId, userId);
        return ResponseEntity.ok(result);
    }
}