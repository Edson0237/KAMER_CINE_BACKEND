package com.kamercinetalents.manager.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête pour le changement de mot de passe volontaire ou forcé
 * (première connexion avec mot de passe temporaire).
 *
 * @param currentPassword le mot de passe actuel (vérifié avant changement)
 * @param newPassword     le nouveau mot de passe (sera hashé par le service)
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 100) String newPassword
) {
}
