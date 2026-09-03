package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création ou modification d'un membre de l'équipe.
 */
public record CreateMembreEquipeRequest(
        @NotBlank String nom,
        @NotBlank String poste,
        String photoUrl,
        String bio,
        int ordre
) {}
