package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.RefreshService;
import Nagasawa.valid_X.domain.dto.LoginResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshService refreshService;

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        // Cookie から refresh_token を取得
        String cookieValue = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refresh_token".equals(c.getName())) {
                    cookieValue = c.getValue();
                    break;
                }
            }
        }
        if (cookieValue == null || cookieValue.isBlank()) {
            return unauthorizedWithCookieClear();
        }

        // UUID 形式の検証
        final UUID refreshId;
        try {
            refreshId = UUID.fromString(cookieValue);
        } catch (IllegalArgumentException e) {
            return unauthorizedWithCookieClear();
        }

        try {
            // リフレッシュ実行（サービス側で回転・失効・新規発行などを実施）
            var result = refreshService.refresh(refreshId);

            // 新しい Refresh Cookie をセットし、アクセストークンと TTL を返却
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, result.jwt())
                    .body(new LoginResponse(result.jwt(), result.accessTtlSecond()));
        } catch (RuntimeException ex) {
            // 失敗時はクッキーを削除して 401（内容は漏らさない）
            return unauthorizedWithCookieClear();
        }
    }

    private static ResponseEntity<LoginResponse> unauthorizedWithCookieClear() {
        String clear = "refresh_token=; Path=/v1/auth; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=None";
        return ResponseEntity.status(401)
                .header(HttpHeaders.SET_COOKIE, clear)
                .build();
    }
}
