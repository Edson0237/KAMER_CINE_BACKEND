package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création ou modification d'une actualité publique.
 */
public record CreateActualiteRequest(
        @NotBlank String titre,
        @NotBlank String contenu,
        String imageUrl
) {}
