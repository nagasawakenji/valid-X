package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.LoginService;
import Nagasawa.valid_X.application.service.OneTimeLoginTokenService;
import Nagasawa.valid_X.domain.dto.AccessIssueResult;
import Nagasawa.valid_X.domain.dto.ConsumeRequest;
import Nagasawa.valid_X.domain.dto.LoginResponse;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth/magic-link")
@RequiredArgsConstructor
public class MagicLinkController {

    private final OneTimeLoginTokenService oneTimeLoginTokenService;
    private final LoginService loginService;
    private final UserMapper userMapper;

    @Value("${app.frontend.base-url}")
    private String appBaseUrl;

    @Value("${app.magic-link.ttl-minutes:10}")
    private int ttlMinutes;

    @PostMapping("/consume")
    public ResponseEntity<LoginResponse> consume(@RequestBody @Valid ConsumeRequest req) {
        Long userId = oneTimeLoginTokenService.consume(req.token());
        if (userId == null) {
            return ResponseEntity.badRequest().body(new LoginResponse("invalid", 0));
        }

        AccessIssueResult result = loginService.issueForUser(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.responseCookie().toString())
                .body(new LoginResponse(result.accessToken(), result.accessTtlSeconds()));
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestMagic(@RequestBody Map<String, String> emailMap) {
        var email = emailMap.get("email");
        var userId = userMapper.findByEmail(email);

        if (userId == null) return ResponseEntity.ok().build();

        oneTimeLoginTokenService.issueLoginLink(userId, Duration.ofMinutes(ttlMinutes), appBaseUrl);
        return ResponseEntity.ok().build();
    }

}
