package com.kamercinetalents.manager.admin.dto;

import java.util.UUID;

/**
 * DTO de consultation d'un feature flag.
 *
 * @param id           l'identifiant unique
 * @param code         le code unique du flag
 * @param libelle      le libellé descriptif
 * @param actif        l'état d'activation
 * @param versionCible la version cible (ex. V2, V3)
 * @param territoireId le territoire ciblé (null = global)
 */
public record FeatureFlagDto(
        UUID id,
        String code,
        String libelle,
        boolean actif,
        String versionCible,
        UUID territoireId
) {
}
