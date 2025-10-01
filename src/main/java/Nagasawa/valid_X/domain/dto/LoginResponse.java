package Nagasawa.valid_X.domain.dto;

public record LoginResponse (
        String accessToken,
        long expiresInSeconds
) {}
