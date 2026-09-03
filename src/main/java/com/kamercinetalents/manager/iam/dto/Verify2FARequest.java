package com.kamercinetalents.manager.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record Verify2FARequest(
        @NotBlank UUID userId,
        @NotBlank @Size(min = 6, max = 6) String code
) {
}
