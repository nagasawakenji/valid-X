package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.SignupService;
import Nagasawa.valid_X.domain.dto.RegisterForm;
import Nagasawa.valid_X.domain.dto.SignupResult;
import Nagasawa.valid_X.domain.dto.SignupStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResult> signup(@RequestBody @Valid RegisterForm form) {
        SignupResult result = signupService.signup(form);

        return switch (result.status()) {
            case ACCEPTED     -> ResponseEntity.accepted().body(result); // 202
            case DUPLICATE    -> ResponseEntity.status(409).body(result); // 409 Conflict
            case RATE_LIMITED -> ResponseEntity.status(429).body(result); // 429 Too Many Requests
            case INVALID_INPUT-> ResponseEntity.badRequest().body(result); // 400
        };
    }
}