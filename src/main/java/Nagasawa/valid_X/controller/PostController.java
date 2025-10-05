package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.PostService;
import Nagasawa.valid_X.domain.dto.PostForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<String> post(@AuthenticationPrincipal Jwt jwt,
                                       @RequestBody @Valid PostForm postForm) {
        Long userId = Long.valueOf(jwt.getSubject());
        String username = jwt.getClaim("username");

        postService.post(userId, postForm);

        return ResponseEntity.ok("userId=" + userId + "username=" + username + "のポストを作成しました");
    }
}
