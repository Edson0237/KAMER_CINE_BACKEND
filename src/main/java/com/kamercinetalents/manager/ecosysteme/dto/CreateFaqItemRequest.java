package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création ou modification d'une question FAQ.
 */
public record CreateFaqItemRequest(
        @NotBlank String question,
        @NotBlank String reponse,
        @NotBlank String categorie,
        int ordre
) {}
