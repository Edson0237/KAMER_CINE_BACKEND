package com.kamercinetalents.manager.iam.dto;

import java.util.UUID;

/**
 * DTO de consultation d'une permission.
 *
 * @param id      l'identifiant unique
 * @param code    le code unique de la permission
 * @param libelle le libellé descriptif
 */
public record PermissionDto(
        UUID id,
        String code,
        String libelle
) {
}
