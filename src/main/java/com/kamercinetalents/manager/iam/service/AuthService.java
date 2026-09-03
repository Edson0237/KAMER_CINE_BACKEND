package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.iam.dto.AuthResponse;
import com.kamercinetalents.manager.iam.dto.LoginRequest;
import com.kamercinetalents.manager.iam.dto.RefreshTokenRequest;

/**
 * Service d'authentification — login, refresh token, logout.
 *
 * <p>Le login vérifie les credentials, charge les permissions dynamiques
 * depuis la base (RBAC), et génère un JWT d'accès + un JWT de rafraîchissement.
 * Le refresh valide le token de rafraîchissement et émet un nouveau token
 * d'accès sans redemander le mot de passe.</p>
 */
public interface AuthService {

    /**
     * Authentifie un utilisateur et émet les tokens JWT.
     *
     * @param request les credentials (email + password)
     * @return les tokens et les informations utilisateur
     */
    Object login(LoginRequest request);

    /**
     * Rafraîchit le token d'accès à partir d'un token de rafraîchissement.
     *
     * @param request le token de rafraîchissement
     * @return de nouveaux tokens d'accès et de rafraîchissement
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Déconnecte l'utilisateur courant (invalidation côté client).
     *
     * <p>Le JWT étant stateless, le logout est principalement une action
     * côté client (suppression du token). Un journal d'audit est écrit
     * pour tracer la déconnexion.</p>
     */
    void logout();
}
