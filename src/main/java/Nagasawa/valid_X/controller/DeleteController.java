package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.DeletePostService;
import Nagasawa.valid_X.domain.dto.DeletePostResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tweets")
@RequiredArgsConstructor
@Validated
public class DeleteController {

    private final DeletePostService deletePostService;

    @DeleteMapping("/{tweetId}/delete")
    public ResponseEntity<DeletePostResult> deletePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId
    ) {
        // Logging start of delete attempt
        if (tweetId == null || tweetId <= 0) {
            org.slf4j.LoggerFactory.getLogger(DeleteController.class)
                    .warn("Invalid tweetId for deletion: {}", tweetId);
            return ResponseEntity.badRequest().build();
        }
        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DeleteController.class)
                    .error("Invalid user id in JWT: {}", jwt != null ? jwt.getSubject() : null, e);
            return ResponseEntity.status(401).build();
        }
        try {
            DeletePostResult result = deletePostService.delete(tweetId, userId);
            org.slf4j.LoggerFactory.getLogger(DeleteController.class)
                    .info("Tweet deleted: tweetId={}, userId={}", tweetId, userId);
            return ResponseEntity.ok(result);
        } catch (Nagasawa.valid_X.exception.notFoundProblems.NotFoundProblemException e) {
            org.slf4j.LoggerFactory.getLogger(DeleteController.class)
                    .warn("Delete failed - tweet not found: tweetId={}, userId={}", tweetId, userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DeleteController.class)
                    .error("Unexpected error during delete: tweetId={}, userId={}", tweetId, userId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
