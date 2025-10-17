package Nagasawa.valid_X.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import Nagasawa.valid_X.application.service.SearchUserService;
import Nagasawa.valid_X.domain.dto.SearchUserSummary;
import Nagasawa.valid_X.domain.dto.Page;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class SearchUserController {

    private final SearchUserService searchUserService;

    @GetMapping("/users/search")
    public ResponseEntity<Page<SearchUserSummary>> searchUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("prefix") String prefix,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "30") int limit) {
        Page<SearchUserSummary> result = searchUserService.searchUser(prefix, cursor, limit);
        return ResponseEntity.ok(result);
    }
}
