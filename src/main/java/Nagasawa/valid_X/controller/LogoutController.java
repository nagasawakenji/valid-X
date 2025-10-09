package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.LogoutService;
import Nagasawa.valid_X.domain.dto.LogoutResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/logout")
public class LogoutController {

    private final LogoutService logoutService;

    @PostMapping()
    public ResponseEntity<LogoutResult> logout(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        String sid = jwt.getClaim("sid");
        UUID sessionUuid = UUID.fromString(sid);

        LogoutResult result = logoutService.logout(userId, sessionUuid);

        // ブラウザの refresh_token Cookie を無効化（Secure / HttpOnly / SameSite=None を維持したまま削除）
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/v1/auth")
                .maxAge(Duration.ZERO) // 即時削除
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(result);
    }
}