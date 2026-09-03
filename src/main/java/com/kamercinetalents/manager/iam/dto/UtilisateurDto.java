package com.kamercinetalents.manager.iam.dto;

import java.util.UUID;

/**
 * DTO de réponse exposant les informations publiques d'un utilisateur.
 *
 * <p>Cet objet est le seul contrat d'échange entre l'API et les clients
 * (web/mobile) pour les données utilisateur. Le hash du mot de passe et
 * les colonnes techniques de synchronisation ne sont jamais sérialisés.</p>
 *
 * @param id           l'UUID de l'utilisateur
 * @param nom          le nom complet
 * @param email        l'adresse email
 * @param telephone    le numéro de téléphone
 * @param actif        indique si le compte est actif
 * @param roleId       l'UUID du rôle
 * @param territoireId l'UUID du territoire de périmètre
 * @param roleCode     le code du rôle (ex. "N7_APPRENANT"), pour affichage direct
 * @param mustChangePassword indique si l'utilisateur doit changer son mot de passe
 */
public record UtilisateurDto(
        UUID id,
        String nom,
        String email,
        String telephone,
        boolean actif,
        UUID roleId,
        UUID territoireId,
        String roleCode,
        boolean mustChangePassword
) {
}
