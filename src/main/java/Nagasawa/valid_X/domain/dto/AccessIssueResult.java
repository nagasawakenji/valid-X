package Nagasawa.valid_X.domain.dto;

import org.springframework.http.ResponseCookie;

public record AccessIssueResult(
        String accessToken,
        long accessTtlSeconds,
        ResponseCookie responseCookie
) {}
