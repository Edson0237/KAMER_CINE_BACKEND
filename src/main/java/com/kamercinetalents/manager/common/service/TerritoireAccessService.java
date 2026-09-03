package com.kamercinetalents.manager.common.service;

import java.util.UUID;

/**
 * Service de contrôle du périmètre territorial.
 *
 * <p>Vérifie qu'un territoire demandé par un utilisateur appartient bien
 * à son périmètre, c'est-à-dire qu'il est soit son propre territoire,
 * soit un descendant dans la hiérarchie (région → département →
 * arrondissement → commune).</p>
 *
 * <p>Le N1 (Comité Central) a accès à tous les territoires. Pour les
 * autres niveaux, la vérification se fait par une requête récursive
 * sur la table {@code territoire} (CTE PostgreSQL).</p>
 *
 * <p>Ce service est appelé par chaque service métier avant de retourner
 * ou modifier des données — le contrôle est appliqué à CHAQUE endpoint,
 * sans exception (règle non négociable du projet).</p>
 */
public interface TerritoireAccessService {

    /**
     * Vérifie que l'utilisateur courant a accès au territoire demandé.
     *
     * @param territoireId l'identifiant du territoire à vérifier
     * @return {@code true} si le territoire est dans le périmètre de l'utilisateur
     */
    boolean canAccess(UUID territoireId);

    /**
     * Vérifie que l'utilisateur courant a accès au territoire demandé
     * et lève une exception si ce n'est pas le cas.
     *
     * @param territoireId l'identifiant du territoire à vérifier
     * @throws com.kamercinetalents.manager.common.exception.PerimeterAccessException si l'accès est refusé
     */
    void requireAccess(UUID territoireId);

    /**
     * Retourne l'identifiant du territoire de périmètre de l'utilisateur courant.
     *
     * @return l'UUID du territoire de l'utilisateur connecté
     */
    UUID getCurrentPerimeter();
}
