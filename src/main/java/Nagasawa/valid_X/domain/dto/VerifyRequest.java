package Nagasawa.valid_X.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
        @NotBlank String token
) {}
