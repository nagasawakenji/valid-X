package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.PostService;
import Nagasawa.valid_X.domain.dto.PostForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = {"multipart/form-data"}) // consumesを指定
    public ResponseEntity<String> post(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("postForm") @Valid PostForm postForm,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        String username = jwt.getClaim("username");
        
        postService.post(userId, postForm, mediaFiles);

        return ResponseEntity.ok("userId=" + userId + "username=" + username + "のポストを作成しました");
    }
}

