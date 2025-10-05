package Nagasawa.valid_X.controller;


import Nagasawa.valid_X.application.service.ReplyService;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.PostResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tweets")
@Validated
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/{tweetId}/reply")
    public ResponseEntity<PostResult> reply(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId,
            @RequestBody @Valid PostForm postForm
            ) {

        if (tweetId == null || tweetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = Long.valueOf(jwt.getSubject());

        PostResult result = replyService.reply(tweetId, userId, postForm);

        return ResponseEntity.ok(result);
    }
}
