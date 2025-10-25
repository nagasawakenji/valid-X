package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.VerifyService;
import Nagasawa.valid_X.domain.dto.VerifyRequest;
import Nagasawa.valid_X.domain.dto.VerifyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class VerifyController {

    private final VerifyService verifyService;

    // フロントからの認証用
    @PostMapping("/verify")
    public ResponseEntity<VerifyResult> verify(@RequestBody Nagasawa.valid_X.domain.dto.VerifyRequest verifyRequest) {
        // VerifyServiceでは認証成功後、after-commit(usersへのINSERT後)にマジックリンクを発行する
        var result = verifyService.verify(verifyRequest.token());
        return ResponseEntity.ok(result);
    }

    // URLクリック用（メール内リンク）
    @GetMapping("/verify")
    public ResponseEntity<VerifyResult> verifyViaUrl(@RequestParam("token") String token) {
        var result = verifyService.verify(token);
        return ResponseEntity.ok(result);
    }

}
