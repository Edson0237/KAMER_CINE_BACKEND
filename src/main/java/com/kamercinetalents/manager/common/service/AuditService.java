package com.kamercinetalents.manager.common.service;

import java.util.Map;
import java.util.UUID;

/**
 * Service d'audit transverse — enregistre chaque action sensible
 * (création, modification, suppression) dans la table {@code audit_log}.
 *
 * <p>Tous les modules (M0 à M13) utilisent ce service pour journaliser
 * leurs opérations. Le journal est polymorphe : {@code entite_type} +
 * {@code entite_id} permettent de tracer n'importe quelle entité sans
 * créer une table de log par module.</p>
 *
 * <p>Ce service est appelé par les implémentations de service métier
 * après chaque opération réussie, dans la même transaction (pour
 * garantir que l'audit et l'opération sont atomiques — règle ACID).</p>
 */
public interface AuditService {

    /**
     * Enregistre une action dans le journal d'audit.
     *
     * @param action     le type d'action ({@code "create"}, {@code "update"}, {@code "delete"})
     * @param entiteType le type d'entité concernée (ex. {@code "territoire"}, {@code "utilisateur"})
     * @param entiteId   l'identifiant unique de l'entité
     * @param details    les détails supplémentaires (changements, contexte) sérialisés en JSON
     */
    void log(String action, String entiteType, UUID entiteId, Map<String, Object> details);
}
