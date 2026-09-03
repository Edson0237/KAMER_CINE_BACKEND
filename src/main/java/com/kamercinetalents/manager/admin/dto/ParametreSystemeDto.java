package com.kamercinetalents.manager.admin.dto;

import java.util.UUID;

/**
 * DTO de consultation d'un paramètre système.
 *
 * @param id                  l'identifiant unique
 * @param cle                 la clé unique du paramètre
 * @param valeur              la valeur actuelle
 * @param type                le type de donnée (string/int/bool/json)
 * @param description         la description fonctionnelle
 * @param modifiableParRoleId l'identifiant du rôle autorisé à modifier
 */
public record ParametreSystemeDto(
        UUID id,
        String cle,
        String valeur,
        String type,
        String description,
        UUID modifiableParRoleId
) {
}
