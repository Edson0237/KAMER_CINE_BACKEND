package com.kamercinetalents.manager.iam.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse après authentification réussie.
 *
 * <p>Contient le token d'accès JWT, le token de rafraîchissement,
 * et les informations de base de l'utilisateur (sans données sensibles).</p>
 *
 * @param accessToken   le token JWT d'accès (court terme)
 * @param refreshToken  le token JWT de rafraîchissement (long terme)
 * @param tokenType     le type de token (toujours "Bearer")
 * @param userId        l'identifiant unique de l'utilisateur
 * @param nom           le nom complet
 * @param email         l'adresse email
 * @param roleCode      le code du rôle
 * @param niveau        le niveau hiérarchique (1 à 7)
 * @param territoireId  l'identifiant du territoire de périmètre
 * @param permissions   la liste des codes de permissions accordées
 * @param mustChangePassword indique si l'utilisateur doit changer son mot de
 *                           passe avant de pouvoir naviguer (compte créé avec
 *                           un mot de passe temporaire — seed admin ou compte
 *                           apprenant généré après acceptation de candidature)
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID userId,
        String nom,
        String email,
        String roleCode,
        int niveau,
        UUID territoireId,
        List<String> permissions,
        boolean mustChangePassword
) {
}
