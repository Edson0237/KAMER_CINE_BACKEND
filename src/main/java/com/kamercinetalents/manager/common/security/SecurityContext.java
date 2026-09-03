package com.kamercinetalents.manager.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * Contexte de sécurité de l'utilisateur authentifié, extrait du JWT.
 *
 * <p>Transporte l'identité complète nécessaire au contrôle d'accès :
 * identifiant utilisateur, code du rôle, niveau hiérarchique (1 à 7),
 * identifiant du territoire de périmètre, et l'ensemble des codes de
 * permissions accordées (RBAC dynamique depuis la base).</p>
 *
 * <p>Cet objet est peuplé par {@link JwtAuthenticationFilter} à partir
 * des claims du token JWT, puis utilisé par les services métier pour
 * vérifier le périmètre territorial et les permissions sans jamais
 * recourir à des rôles codés en dur (principe DIP — la source de
 * vérité est la base, pas le code).</p>
 */
public record SecurityContext(
        UUID userId,
        String roleCode,
        int niveauHierarchique,
        UUID territoireId,
        Set<String> permissions
) {

    /**
     * Vérifie si l'utilisateur possède une permission donnée.
     *
     * @param code le code de la permission (ex. {@code "territoire:write"})
     * @return {@code true} si la permission est accordée
     */
    public boolean hasPermission(String code) {
        return permissions != null && permissions.contains(code);
    }
}
