package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création ou modification d'un partenaire.
 */
public record CreatePartenaireRequest(
        @NotBlank String nom,
        String logoUrl,
        String siteWeb,
        int ordre
) {}
