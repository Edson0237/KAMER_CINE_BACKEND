package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création d'un message de contact (formulaire public).
 */
public record CreateContactMessageRequest(
        @NotBlank String nom,
        @NotBlank @Email String email,
        @NotBlank String sujet,
        @NotBlank String message
) {}
