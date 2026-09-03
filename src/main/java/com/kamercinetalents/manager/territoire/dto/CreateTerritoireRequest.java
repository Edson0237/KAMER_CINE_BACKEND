package com.kamercinetalents.manager.territoire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * DTO de requête pour la création d'un territoire.
 *
 * @param code            le code unique du territoire (ex. REG_CENTRE)
 * @param nom             le nom du territoire (ex. "Centre")
 * @param typeTerritoireId l'UUID du type de territoire
 * @param parentId        l'UUID du territoire parent (null pour la racine)
 * @param statutCommuneId l'UUID du statut de commune (optionnel)
 * @param metadata        métadonnées JSONB additionnelles (optionnel)
 */
public record CreateTerritoireRequest(
        @NotBlank String code,
        @NotBlank String nom,
        @NotNull UUID typeTerritoireId,
        UUID parentId,
        UUID statutCommuneId,
        Map<String, Object> metadata
) {
}
