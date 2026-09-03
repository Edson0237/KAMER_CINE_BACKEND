package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création d'une candidature publique (formulaire d'inscription).
 */
public record CreateCandidatureRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank @Email String email,
        String telephone,
        String motivation
) {}
