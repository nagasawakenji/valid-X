// CsrfController.java
package Nagasawa.valid_X.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {

    // XSRF-TOKENをcookieにセットするためのエンドポイント
    // ログイン完了時に自動的に実行されるようにする
    @GetMapping("/v1/auth/csrf")
    public Map<String, String> getCsrfToken(CsrfToken token) {
        return Map.of("csrfToken", token.getToken());
    }
}