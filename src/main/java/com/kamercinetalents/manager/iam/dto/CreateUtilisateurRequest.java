package com.kamercinetalents.manager.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO de requête pour la création d'un utilisateur.
 *
 * <p>Utilisé par le Comité Central (N1) pour créer un compte utilisateur
 * avec un rôle et un territoire assignés. Les validations Bean Validation
 * garantissent l'intégrité des données avant d'atteindre la couche service.</p>
 *
 * @param nom          le nom complet (obligatoire)
 * @param email        l'adresse email unique (obligatoire, format validé)
 * @param password     le mot de passe en clair (obligatoire, sera hashé par le service)
 * @param telephone    le numéro de téléphone (optionnel)
 * @param roleId       l'UUID du rôle à assigner (obligatoire)
 * @param territoireId l'UUID du territoire de périmètre (obligatoire)
 */
public record CreateUtilisateurRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 20) String telephone,
        @NotNull UUID roleId,
        UUID territoireId
) {
}
