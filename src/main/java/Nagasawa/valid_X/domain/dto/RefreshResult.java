package Nagasawa.valid_X.domain.dto;

import org.springframework.http.ResponseCookie;

public record RefreshResult (
    String jwt,
    long accessTtlSecond,
    ResponseCookie cookie
) {}
