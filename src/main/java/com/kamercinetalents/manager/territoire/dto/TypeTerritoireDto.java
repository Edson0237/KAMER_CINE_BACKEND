package com.kamercinetalents.manager.territoire.dto;

import java.util.UUID;

/**
 * DTO de consultation d'un type de territoire.
 *
 * @param id      l'identifiant unique
 * @param code    le code unique (ex. PAYS, REGION, DEPARTEMENT, ARRONDISSEMENT, COMMUNE)
 * @param libelle le libellé descriptif
 * @param niveau  le niveau hiérarchique (1 = Pays, 2 = Région, etc.)
 */
public record TypeTerritoireDto(
        UUID id,
        String code,
        String libelle,
        short niveau
) {
}
