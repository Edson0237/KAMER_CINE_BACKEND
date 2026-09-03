package com.kamercinetalents.manager.territoire.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de consultation d'un territoire.
 *
 * @param id              l'identifiant unique
 * @param code            le code unique
 * @param nom             le nom du territoire
 * @param typeTerritoireId l'UUID du type de territoire
 * @param parentId        l'UUID du territoire parent (null = racine)
 * @param statutCommuneId l'UUID du statut de commune (optionnel)
 * @param metadata        métadonnées JSONB
 * @param deletedAt       date de suppression douce (null = actif)
 */
public record TerritoireDto(
        UUID id,
        String code,
        String nom,
        UUID typeTerritoireId,
        UUID parentId,
        UUID statutCommuneId,
        Map<String, Object> metadata,
        OffsetDateTime deletedAt
) {
}
