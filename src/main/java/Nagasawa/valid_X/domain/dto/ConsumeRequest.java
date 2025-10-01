package Nagasawa.valid_X.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsumeRequest (
        @NotBlank String token
) {}
