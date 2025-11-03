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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tweets")
@Validated
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping(value = "/{tweetId}/reply", consumes = {"multipart/form-data"}) // consumesを指定
    public ResponseEntity<PostResult> reply(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("tweetId") Long tweetId,
            @RequestPart("postForm") @Valid PostForm postForm,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles // ★ 追加
    ) {

        if (tweetId == null || tweetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = Long.valueOf(jwt.getSubject());

        // ★ Serviceのシグネチャを変更し、mediaFilesを渡す
        PostResult result = replyService.reply(tweetId, userId, postForm, mediaFiles);

        return ResponseEntity.ok(result);
    }
}
