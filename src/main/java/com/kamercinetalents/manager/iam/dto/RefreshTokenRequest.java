package com.kamercinetalents.manager.iam.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour le rafraîchissement du token.
 *
 * @param refreshToken le token de rafraîchissement JWT
 */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
