package com.kamercinetalents.manager.iam.dto;

import java.util.UUID;

/**
 * DTO de consultation d'un rôle.
 *
 * @param id                  l'identifiant unique
 * @param code                le code unique du rôle
 * @param libelle             le libellé descriptif
 * @param niveauHierarchique  le niveau (1 à 7)
 */
public record RoleDto(
        UUID id,
        String code,
        String libelle,
        short niveauHierarchique
) {
}
