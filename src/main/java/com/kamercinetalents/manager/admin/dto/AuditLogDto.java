package com.kamercinetalents.manager.admin.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de consultation d'une entrée du journal d'audit.
 *
 * <p>Exposé en lecture seule — aucune modification n'est possible
 * via l'API. Les détails sont sérialisés en JSON transparent.</p>
 *
 * @param id           l'identifiant unique de l'entrée
 * @param utilisateurId l'identifiant de l'utilisateur ayant effectué l'action
 * @param action       le type d'action (create/update/delete)
 * @param entiteType   le type d'entité concernée
 * @param entiteId     l'identifiant de l'entité concernée
 * @param date         la date et heure de l'action
 * @param details      les détails supplémentaires (JSON)
 */
public record AuditLogDto(
        UUID id,
        UUID utilisateurId,
        String action,
        String entiteType,
        UUID entiteId,
        OffsetDateTime date,
        Map<String, Object> details
) {
}
