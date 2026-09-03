package com.kamercinetalents.manager.iam.dto;

import java.util.UUID;

/**
 * Réponse de login quand l'utilisateur a activé la 2FA.
 * Le client doit appeler /verify-2fa avec le code pour obtenir les tokens JWT.
 */
public record Login2FAResponse(
        boolean twoFactorRequired,
        UUID userId,
        String email
) {
}
