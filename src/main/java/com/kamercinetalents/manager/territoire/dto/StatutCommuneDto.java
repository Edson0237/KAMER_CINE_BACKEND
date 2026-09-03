package com.kamercinetalents.manager.territoire.dto;

import java.util.UUID;

/**
 * DTO de consultation d'un statut de commune.
 *
 * @param id      l'identifiant unique
 * @param code    le code unique (ex. ACTIF, SUSPENDU, EN_ATTENTE)
 * @param libelle le libellé descriptif
 */
public record StatutCommuneDto(
        UUID id,
        String code,
        String libelle
) {
}
