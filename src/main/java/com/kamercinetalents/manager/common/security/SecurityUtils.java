package com.kamercinetalents.manager.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utilitaire d'accès au {@link SecurityContext} de l'utilisateur courant.
 *
 * <p>Centralise l'extraction du contexte depuis le {@link SecurityContextHolder}
 * de Spring Security. Les services métier appellent {@link #get()} pour
 * obtenir l'identité, le rôle, le territoire de périmètre et les permissions
 * sans manipuler directement l'API Spring Security.</p>
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Extrait le contexte de sécurité de l'utilisateur authentifié.
     *
     * @return le {@link SecurityContext} de l'utilisateur courant
     * @throws IllegalStateException si aucun utilisateur n'est authentifié
     */
    public static SecurityContext get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityContext ctx)) {
            throw new IllegalStateException("Aucun utilisateur authentifié dans le contexte de sécurité");
        }
        return ctx;
    }

    /**
     * Retourne l'UUID du territoire de périmètre de l'utilisateur courant.
     *
     * @return l'identifiant du territoire, ou {@code null} si l'utilisateur
     *         n'a pas de territoire assigné (cas exceptionnel)
     */
    public static UUID getCurrentTerritoireId() {
        return get().territoireId();
    }

    /**
     * Retourne l'UUID de l'utilisateur courant.
     *
     * @return l'identifiant unique de l'utilisateur authentifié
     */
    public static UUID getCurrentUserId() {
        return get().userId();
    }
}
